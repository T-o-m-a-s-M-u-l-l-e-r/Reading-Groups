package com.muller_tomas.reading_groups.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.muller_tomas.reading_groups.dto.GroupCreationRequest;
import com.muller_tomas.reading_groups.dto.GroupResponse;
import com.muller_tomas.reading_groups.dto.MapperHelper;
import com.muller_tomas.reading_groups.dto.UserResponse;
import com.muller_tomas.reading_groups.exception.GroupNotFoundException;
import com.muller_tomas.reading_groups.exception.UserNotFoundException;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.GroupRepository;
import com.muller_tomas.reading_groups.repository.UserRepository;

@Service
public class GroupService {
	private final GroupRepository groupRepository;
	private final UserRepository userRepository;
	private final MapperHelper mapperHelper;
	private final ReadingTextService readingTextService;
	private final UserService userService;

	public GroupService(GroupRepository groupRepository, UserRepository userRepository, MapperHelper mapperHelper,
			ReadingTextService readingTextService, UserService userService) {
		this.groupRepository = groupRepository;
		this.userRepository = userRepository;
		this.mapperHelper = mapperHelper;
		this.readingTextService = readingTextService;
		this.userService = userService;
	}

	public Set<GroupResponse> getUserGroups(int userId) throws UserNotFoundException {
		userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with specified id not found"));
		Set<Group> groups = groupRepository.findByUsersId(userId);
		return mapperHelper.mapGroups(groups);
	}

	public Set<UserResponse> getGroupMembers(int userId, int groupId) throws GroupNotFoundException {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new GroupNotFoundException("Group with specified id not found"));
		Set<User> users = group.getUsers();
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with specified id not found"));

		if (!users.contains(user)) {
			throw new BadCredentialsException("User not authenticated to view group");
		}

		return mapperHelper.mapUsers(users);
	}

	public Resource getReadingText(int userId, int groupId) {
		User user = userService.findUserByUserId(userId);
		Group group = findGroupById(groupId);
		
		if (!group.getUsers().contains(user)) {
			throw new AuthorizationDeniedException("You do not have access to this group");
		}

		return readingTextService.load(group.getReadingTextPath());
	}

	public GroupResponse createGroup(int userId, GroupCreationRequest groupCreationRequest)
			throws UserNotFoundException {
		User administratorUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User with specified id not found"));
		String filePath = readingTextService.save(groupCreationRequest.getReadingText());
		HashSet<User> users = new HashSet<User>();
		users.add(administratorUser);
		Group group = groupRepository
				.save(new Group(filePath, administratorUser, groupCreationRequest.getGroupName(), users));
		return mapperHelper.mapGroup(group);
	}

	@Transactional
	public GroupResponse addMember(int groupId, int userId) {
		Group group = groupRepository.findByIdForUpdate(groupId)
				.orElseThrow(() -> new GroupNotFoundException("Group with specified id not found"));
		User user = userService.findUserByUserId(userId);

		if (group.getUsers().add(user)) {
			groupRepository.save(group);
		}

		return mapperHelper.mapGroup(group);
	}

	public Group findGroupById(int groupId) {
		return groupRepository.findById(groupId)
				.orElseThrow(() -> new GroupNotFoundException("Group with specified id not found"));
	}

}
