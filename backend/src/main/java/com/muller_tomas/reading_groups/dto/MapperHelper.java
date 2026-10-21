package com.muller_tomas.reading_groups.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.muller_tomas.reading_groups.model.Comment;
import com.muller_tomas.reading_groups.model.Group;
import com.muller_tomas.reading_groups.model.Invite;
import com.muller_tomas.reading_groups.model.User;

@Component
public class MapperHelper {

	public Set<GroupResponse> mapGroups(Set<Group> groups) {
		Set<GroupResponse> groupResponses = groups.stream().map(group -> mapGroup(group)).collect(Collectors.toSet());
		return groupResponses;
	}

	public GroupResponse mapGroup(Group group) {
		GroupResponse response = new GroupResponse(group.getId(), group.getAdministratorUser().getId(),
				group.getCreatedAt(),
				group.getGroupName(), group.getUsers().size(), group.getAdministratorUser().getUsername());
		response.setHasIcon(group.getIconPath() != null);
		return response;
	}

	public Set<UserResponse> mapUsers(Set<User> users) {
		Set<UserResponse> userResponses = users.stream().map(user -> new UserResponse(user.getId(), user.getUsername()))
				.collect(Collectors.toSet());
		return userResponses;
	}

	public Set<InviteResponse> mapInvites(Set<Invite> invites) {
		Set<InviteResponse> inviteResponses = invites.stream().map(invite -> new InviteResponse(invite.getInviteId(),
				invite.getGroup().getId(), invite.getRecipient().getId())).collect(Collectors.toSet());
		return inviteResponses;
	}

	public CommentResponse mapComment(Comment comment) {
		return new CommentResponse(comment.getCommentId(), comment.getGroup().getId(),
				comment.getCategory() != null ? comment.getCategory().getCategoryId() : null, comment.getUser().getId(),
				comment.getTextSection(), comment.getCommentContent(), comment.getCreatedAt(), comment.getUpdatedAt(),
				comment.getCommentPageNumber(), comment.getCommentPageOccurrence(), comment.getUser().getUsername(),
				comment.getUser().getId());
	}

	public Set<CommentResponse> mapComments(Set<Comment> comments) {
		Set<CommentResponse> commentResponses = comments.stream().map(comment -> mapComment(comment))
				.collect(Collectors.toSet());
		return commentResponses;
	}

}
