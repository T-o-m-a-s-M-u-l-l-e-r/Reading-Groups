package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import com.muller_tomas.reading_groups.dto.GroupCreationRequest;
import com.muller_tomas.reading_groups.dto.MapperHelper;
import com.muller_tomas.reading_groups.exception.InvalidGroupIconException;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.GroupRepository;
import com.muller_tomas.reading_groups.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private MapperHelper mapperHelper;
	@Mock
	private ReadingTextService readingTextService;
	@Mock
	private UserService userService;
	@Mock
	private GroupIconService groupIconService;
	private GroupService groupService;

	@BeforeEach
	public void setUp() {
		groupService = new GroupService(groupRepository, userRepository, mapperHelper, readingTextService, userService,
				groupIconService);
	}

	@Test
	public void creationPersistsIconReference() {
		GroupCreationRequest request = new GroupCreationRequest();
		request.setGroupName("Readers");
		when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
		when(readingTextService.save(null)).thenReturn("pdf-id");
		when(groupIconService.save(null)).thenReturn("icon-id");
		when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

		groupService.createGroup(1, request);

		ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
		verify(groupRepository).save(groupCaptor.capture());
		assertEquals("icon-id", groupCaptor.getValue().getIconPath());
	}

	@Test
	public void invalidIconCleansUpPdfWithoutCreatingGroup() {
		when(userRepository.findById(1)).thenReturn(Optional.of(new User()));
		when(readingTextService.save(null)).thenReturn("pdf-id");
		when(groupIconService.save(null)).thenThrow(new InvalidGroupIconException("Invalid icon"));

		assertThrows(InvalidGroupIconException.class, () -> groupService.createGroup(1, new GroupCreationRequest()));

		verify(readingTextService).delete("pdf-id");
		verify(groupRepository, never()).save(any(Group.class));
	}

	@Test
	public void nonMembersCannotLoadIcon() {
		when(groupRepository.findById(7)).thenReturn(Optional.of(new Group()));
		when(userService.findUserByUserId(2)).thenReturn(new User());

		assertThrows(AuthorizationDeniedException.class, () -> groupService.getIcon(2, 7));

		verify(groupIconService, never()).load(any());
	}
}
