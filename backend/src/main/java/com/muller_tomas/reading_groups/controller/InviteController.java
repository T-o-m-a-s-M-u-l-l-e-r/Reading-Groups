package com.muller_tomas.reading_groups.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.muller_tomas.reading_groups.dto.InviteResponse;
import com.muller_tomas.reading_groups.service.InviteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Invites", description = "Endpoints for invite operations")
@RestController
public class InviteController {
	private final InviteService inviteService;

	public InviteController(InviteService inviteService) {
		this.inviteService = inviteService;
	}
	@GetMapping("/api/invites")
	@Operation(summary = "Get my invitations", description = "Retrieve pending group invitations addressed to the authenticated user.")
	@ApiResponse(responseCode = "200", description = "Invitations returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	public ResponseEntity<Set<InviteResponse>> getUserInvites(@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok(inviteService.getUserInvites(userId));
	}
	@PostMapping("/api/groups/{groupId}/invites")
	@Operation(summary = "Invite user to group", description = "Create an invitation for the user identified by id. Only the group administrator can send invitations.")
	@ApiResponse(responseCode = "201", description = "Invitation created")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User is not the group administrator")
	@ApiResponse(responseCode = "404", description = "Group or user not found")
	public ResponseEntity<InviteResponse> sendInvite(@PathVariable int groupId, @RequestParam int userId,
			@AuthenticationPrincipal Integer administratorId) {
		InviteResponse inviteResponse = inviteService.createInvite(administratorId, groupId, userId);
		return new ResponseEntity<InviteResponse>(inviteResponse, HttpStatus.CREATED);
	}
	@DeleteMapping("/api/invites/{inviteId}")
	@Operation(summary = "Reject invitation", description = "Delete an invitation addressed to the authenticated user without joining the group.")
	@ApiResponse(responseCode = "204", description = "Invitation rejected")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "Invitation belongs to another user")
	@ApiResponse(responseCode = "404", description = "Invitation not found")
	public ResponseEntity<Void> rejectInvite(@PathVariable int inviteId,
			@AuthenticationPrincipal Integer userId) {
		inviteService.deleteInvite(userId, inviteId);
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/api/invites/{inviteId}/accept")
	@Operation(summary = "Accept invitation", description = "Join the group associated with an invitation addressed to the authenticated user and remove the invitation.")
	@ApiResponse(responseCode = "204", description = "Invitation accepted")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "Invitation belongs to another user")
	@ApiResponse(responseCode = "404", description = "Invitation not found")
	public ResponseEntity<Void> acceptInvite(@PathVariable int inviteId,
			@AuthenticationPrincipal Integer userId) {
		inviteService.acceptInvite(userId, inviteId);
		return ResponseEntity.noContent().build();
	}

}
