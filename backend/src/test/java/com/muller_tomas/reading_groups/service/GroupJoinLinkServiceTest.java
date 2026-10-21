package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.muller_tomas.reading_groups.dto.GroupJoinPreviewResponse;
import com.muller_tomas.reading_groups.exception.InvalidGroupJoinLinkException;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.GroupJoinLink;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.GroupJoinLinkRepository;
import com.muller_tomas.reading_groups.repository.GroupRepository;

@ExtendWith(MockitoExtension.class)
public class GroupJoinLinkServiceTest {
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private GroupJoinLinkRepository linkRepository;
	@Mock
	private GroupService groupService;
	@Mock
	private GroupIconService iconService;
	private GroupJoinLinkService service;
	private final String token = "a".repeat(43);
	private String hash;
	private Group group;

	@BeforeEach
	public void setUp() throws Exception {
		service = new GroupJoinLinkService(groupRepository, linkRepository, groupService, iconService,
				"https://example.test/join");
		hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(token.getBytes(StandardCharsets.UTF_8)));
		group = new Group();
		group.setId(7);
		group.setGroupName("Readers");
		User administrator = new User();
		administrator.setUsername("Reader");
		group.setAdministratorUser(administrator);
		group.getUsers().add(administrator);
	}

	private void stubLink(String storedHash, Instant expiresAt) {
		when(linkRepository.findGroupIdByTokenHash(hash)).thenReturn(Optional.of(7));
		when(groupRepository.findByIdForUpdate(7)).thenReturn(Optional.of(group));
		when(linkRepository.findById(7)).thenReturn(Optional.of(
				new GroupJoinLink(group, storedHash, Instant.now().minusSeconds(60), expiresAt)));
	}

	@Test
	public void previewReturnsGroupSummaryWithoutAddingMember() {
		stubLink(hash, Instant.now().plusSeconds(60));
		GroupJoinPreviewResponse preview = service.getPreview(token);
		assertEquals("Readers", preview.getGroupName());
		assertEquals("Reader", preview.getAdministratorName());
		assertEquals(1, preview.getMemberCount());
		verify(groupService, never()).addMember(anyInt(), anyInt());
	}

	@Test
	public void expiredOrReplacedLinkCannotBePreviewed() {
		stubLink(hash, Instant.now().minusSeconds(1));
		assertThrows(InvalidGroupJoinLinkException.class, () -> service.getPreview(token));
		stubLink("different-hash", Instant.now().plusSeconds(60));
		assertThrows(InvalidGroupJoinLinkException.class, () -> service.getPreviewIcon(token));
		verify(iconService, never()).load(any());
	}

	@Test
	public void revokedOrMalformedLinkCannotBePreviewed() {
		assertThrows(InvalidGroupJoinLinkException.class, () -> service.getPreview("bad-token"));
		when(linkRepository.findGroupIdByTokenHash(hash)).thenReturn(Optional.of(7));
		when(groupRepository.findByIdForUpdate(7)).thenReturn(Optional.of(group));
		when(linkRepository.findById(7)).thenReturn(Optional.empty());
		assertThrows(InvalidGroupJoinLinkException.class, () -> service.getPreview(token));
	}

	@Test
	public void validLinkAllowsIconPreviewAndJoiningStillAddsMember() {
		stubLink(hash, Instant.now().plusSeconds(60));
		group.setIconPath("icon-id");
		service.getPreviewIcon(token);
		verify(iconService).load("icon-id");
		service.joinGroup(token, 9);
		verify(groupService).addMember(7, 9);
	}
}
