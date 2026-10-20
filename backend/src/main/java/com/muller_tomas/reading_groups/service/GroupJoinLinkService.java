package com.muller_tomas.reading_groups.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.muller_tomas.reading_groups.dto.CreateGroupJoinLinkRequest;
import com.muller_tomas.reading_groups.dto.CreateGroupJoinLinkResponse;
import com.muller_tomas.reading_groups.dto.GroupJoinLinkResponse;
import com.muller_tomas.reading_groups.dto.GroupResponse;
import com.muller_tomas.reading_groups.exception.GroupNotFoundException;
import com.muller_tomas.reading_groups.exception.InvalidGroupJoinLinkException;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.GroupJoinLink;
import com.muller_tomas.reading_groups.repository.GroupJoinLinkRepository;
import com.muller_tomas.reading_groups.repository.GroupRepository;

@Service
public class GroupJoinLinkService {
	private final GroupRepository groupRepository;
	private final GroupJoinLinkRepository groupJoinLinkRepository;
	private final GroupService groupService;
	private final String joinPageUrl;
	private final SecureRandom secureRandom = new SecureRandom();

	public GroupJoinLinkService(GroupRepository groupRepository, GroupJoinLinkRepository groupJoinLinkRepository,
			GroupService groupService, @Value("${app.group-join-link.page-url}") String joinPageUrl) {
		this.groupRepository = groupRepository;
		this.groupJoinLinkRepository = groupJoinLinkRepository;
		this.groupService = groupService;
		this.joinPageUrl = joinPageUrl;
	}

	@Transactional
	public CreateGroupJoinLinkResponse createLink(int groupId, int userId, CreateGroupJoinLinkRequest request) {
		Group group = lockGroup(groupId);
		requireAdministrator(group, userId);
		if (request == null || request.getValidity() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link validity is required");
		}

		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
		String tokenHash = hashToken(token);
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(request.getValidity().getDuration());

		GroupJoinLink groupJoinLink = groupJoinLinkRepository.findById(groupId).orElse(null);
		if (groupJoinLink == null) {
			groupJoinLink = new GroupJoinLink(group, tokenHash, issuedAt, expiresAt);
		} else {
			groupJoinLink.replaceToken(tokenHash, issuedAt, expiresAt);
		}
		groupJoinLinkRepository.save(groupJoinLink);

		String url = UriComponentsBuilder.fromUriString(joinPageUrl)
				.pathSegment(token)
				.build()
				.encode()
				.toUriString();
		return new CreateGroupJoinLinkResponse(url, expiresAt);
	}

	@Transactional
	public GroupJoinLinkResponse getLink(int groupId, int userId) {
		Group group = lockGroup(groupId);
		requireAdministrator(group, userId);
		GroupJoinLink groupJoinLink = groupJoinLinkRepository.findById(groupId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group has no join link"));
		boolean expired = !groupJoinLink.getExpiresAt().isAfter(Instant.now());
		return new GroupJoinLinkResponse(groupJoinLink.getIssuedAt(), groupJoinLink.getExpiresAt(), expired);
	}

	@Transactional
	public void revokeLink(int groupId, int userId) {
		Group group = lockGroup(groupId);
		requireAdministrator(group, userId);
		GroupJoinLink groupJoinLink = groupJoinLinkRepository.findById(groupId).orElse(null);
		if (groupJoinLink != null) {
			groupJoinLinkRepository.delete(groupJoinLink);
		}
	}

	@Transactional
	public GroupResponse joinGroup(String token, int userId) {
		if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
			throw new InvalidGroupJoinLinkException();
		}

		String tokenHash = hashToken(token);
		int groupId = groupJoinLinkRepository.findGroupIdByTokenHash(tokenHash)
				.orElseThrow(() -> new InvalidGroupJoinLinkException());
		lockGroup(groupId);

		// Reload only after locking: the link may have been replaced or revoked while we waited.
		GroupJoinLink groupJoinLink = groupJoinLinkRepository.findById(groupId)
				.orElseThrow(() -> new InvalidGroupJoinLinkException());
		if (!tokenHash.equals(groupJoinLink.getTokenHash())
				|| !groupJoinLink.getExpiresAt().isAfter(Instant.now())) {
			throw new InvalidGroupJoinLinkException();
		}

		// This joins the same transaction, retaining the group lock until membership is saved.
		return groupService.addMember(groupId, userId);
	}

	private Group lockGroup(int groupId) {
		return groupRepository.findByIdForUpdate(groupId)
				.orElseThrow(() -> new GroupNotFoundException("Group with specified id not found"));
	}

	private void requireAdministrator(Group group, int userId) {
		if (group.getAdministratorUser().getId() != userId) {
			throw new AuthorizationDeniedException("You are not the administrator of the group");
		}
	}

	private String hashToken(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
