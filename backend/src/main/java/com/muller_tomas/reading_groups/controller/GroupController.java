package com.muller_tomas.reading_groups.controller;

import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.muller_tomas.reading_groups.dto.GroupCreationRequest;
import com.muller_tomas.reading_groups.dto.GroupResponse;
import com.muller_tomas.reading_groups.dto.UserResponse;
import com.muller_tomas.reading_groups.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Groups", description = "Endpoints for group management")
@RestController
public class GroupController {
	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}
	@GetMapping("/api/groups")
	@Operation(summary = "Get my groups", description = "Retrieve the reading groups the authenticated user belongs to.")
	@ApiResponse(responseCode = "200", description = "Groups returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "404", description = "User not found")
	public ResponseEntity<Set<GroupResponse>> getUserGroups(@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok(groupService.getUserGroups(userId));
	}
	@GetMapping("/api/groups/{groupId}/reading-text")
	@Operation(summary = "Get reading text", description = "Retrieve the PDF reading text associated with a group. The response is an inline application/pdf resource; group access is checked.")
	@ApiResponse(responseCode = "200", description = "PDF returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User is not a group member")
	@ApiResponse(responseCode = "404", description = "Group or stored PDF not found")
	public ResponseEntity<Resource> getGroupReadingText(@PathVariable int groupId,
			@AuthenticationPrincipal Integer userId) {
		Resource resource = groupService.getReadingText(userId, groupId);
		return ResponseEntity.ok()
		        .contentType(MediaType.APPLICATION_PDF)
		        .header(
		                HttpHeaders.CONTENT_DISPOSITION,
		                "inline; filename=\"reading-text.pdf\""
		        )
		        .body(resource);
	}
	@GetMapping("/api/groups/{groupId}/members")
	@Operation(summary = "Get group members", description = "Retrieve the members of a group accessible to the authenticated user.")
	@ApiResponse(responseCode = "200", description = "Members returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User is not a group member")
	@ApiResponse(responseCode = "404", description = "User or group not found")
	public ResponseEntity<Set<UserResponse>> getGroupMembers(@PathVariable int groupId,
			@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok(groupService.getGroupMembers(userId, groupId));
	}
	@PostMapping(path = "/api/groups", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Create reading group", description = "Create a group with a name and uploaded PDF reading text using multipart/form-data. The authenticated user becomes the administrator.")
	@ApiResponse(responseCode = "201", description = "Group created")
	@ApiResponse(responseCode = "400", description = "Invalid request or PDF")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "500", description = "PDF could not be stored")
	public ResponseEntity<GroupResponse> createGroup(@Valid @ModelAttribute GroupCreationRequest groupCreationRequest,
			@AuthenticationPrincipal Integer userId) {
		GroupResponse groupResponse = groupService.createGroup(userId, groupCreationRequest);
		return new ResponseEntity<GroupResponse>(groupResponse, HttpStatus.CREATED);
	}

}
