package com.muller_tomas.reading_groups.service;

import java.util.Set;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.muller_tomas.reading_groups.dto.InviteResponse;
import com.muller_tomas.reading_groups.dto.MapperHelper;
import com.muller_tomas.reading_groups.exception.InviteNotFoundException;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.Invite;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.InviteRepository;

@Service
public class InviteService {
	private InviteRepository inviteRepository;
	private MapperHelper mapperHelper;
	private UserService userService;
	private GroupService groupService;
	
	public InviteService(InviteRepository inviteRepository, MapperHelper mapperHelper, UserService userService,
			GroupService groupService) {
		this.inviteRepository = inviteRepository;
		this.mapperHelper = mapperHelper;
		this.userService = userService;
		this.groupService = groupService;
	}

	public Set<InviteResponse> getUserInvites(int userId) {
		Set<Invite> invites = inviteRepository.findAllByRecipient_Id(userId);
		return mapperHelper.mapInvites(invites);
	}
	
	public InviteResponse createInvite(int senderId, int groupId, int recipientId) {
		User recipient = userService.findUserByUserId(recipientId);
		User sender = userService.findUserByUserId(senderId);
		Group group = groupService.findGroupById(groupId);
		
		if (!group.getAdministratorUser().equals(sender)) {
			throw new AuthorizationDeniedException("You are not the administrator of the group");
		}
		
		Invite createdInvite = new Invite();
		createdInvite.setGroup(group);
		createdInvite.setRecipient(recipient);
		
		Invite savedInvite = inviteRepository.save(createdInvite);
		return new InviteResponse(savedInvite.getInviteId(), savedInvite.getGroup().getId(), savedInvite.getRecipient().getId());
	}
	
	public void deleteInvite(int recipientId, int inviteId) {
		User recipient = userService.findUserByUserId(recipientId);
		Invite invite = findInviteByInviteId(inviteId);
		
		if (!invite.getRecipient().equals(recipient)) {
			throw new AuthorizationDeniedException("You are not the recipient of this invite");
		}
		
		inviteRepository.delete(invite);
	}
	
	@Transactional
	public void acceptInvite(int recipientId, int inviteId) {
		Invite invite = findInviteByInviteId(inviteId);
		User recipient = userService.findUserByUserId(recipientId);
		
		if (invite.getRecipient().getId() != recipientId) {
			throw new AuthorizationDeniedException("You are not the recipient of this invite");
		}
		
		Group group = invite.getGroup();
		groupService.addMember(group.getId(), recipient.getId());
		
		inviteRepository.delete(invite);
	}
	
	public Invite findInviteByInviteId(int inviteId) {
		return inviteRepository.findById(inviteId).orElseThrow(() -> new InviteNotFoundException("Invite with specified id not found"));
	}

}
