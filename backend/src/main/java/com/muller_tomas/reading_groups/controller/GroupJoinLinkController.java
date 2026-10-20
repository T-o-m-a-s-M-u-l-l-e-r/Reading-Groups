package com.muller_tomas.reading_groups.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.muller_tomas.reading_groups.dto.CreateGroupJoinLinkRequest;
import com.muller_tomas.reading_groups.dto.CreateGroupJoinLinkResponse;
import com.muller_tomas.reading_groups.dto.GroupJoinLinkResponse;
import com.muller_tomas.reading_groups.dto.GroupResponse;
import com.muller_tomas.reading_groups.service.GroupJoinLinkService;

@RestController
@Tag(name = "Group join links")
public class GroupJoinLinkController {
	private final GroupJoinLinkService groupJoinLinkService;

	public GroupJoinLinkController(GroupJoinLinkService groupJoinLinkService) {
		this.groupJoinLinkService = groupJoinLinkService;
	}

	@PostMapping("/api/groups/{groupId}/join-link")
	@Operation(summary = "Create or replace a group join link",
			description = "Administrator only. The complete link is returned once; any previous link stops working.")
	public ResponseEntity<CreateGroupJoinLinkResponse> createLink(@PathVariable int groupId,
			@AuthenticationPrincipal Integer userId, @Valid @RequestBody CreateGroupJoinLinkRequest request) {
		CreateGroupJoinLinkResponse response = groupJoinLinkService.createLink(groupId, userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(response);
	}

	@GetMapping("/api/groups/{groupId}/join-link")
	@Operation(summary = "Get group join link metadata", description = "Administrator only. Does not return the token.")
	public ResponseEntity<GroupJoinLinkResponse> getLink(@PathVariable int groupId,
			@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore())
				.body(groupJoinLinkService.getLink(groupId, userId));
	}

	@DeleteMapping("/api/groups/{groupId}/join-link")
	@Operation(summary = "Revoke a group join link", description = "Administrator only. Safe to repeat.")
	public ResponseEntity<Void> revokeLink(@PathVariable int groupId, @AuthenticationPrincipal Integer userId) {
		groupJoinLinkService.revokeLink(groupId, userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/api/groups/join/{token}")
	@Operation(summary = "Join a group using a link", description = "Requires login. The link remains reusable.")
	public ResponseEntity<GroupResponse> joinGroup(@PathVariable String token,
			@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok(groupJoinLinkService.joinGroup(token, userId));
	}
}
