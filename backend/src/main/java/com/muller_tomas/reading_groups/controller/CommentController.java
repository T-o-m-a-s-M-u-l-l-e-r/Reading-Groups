package com.muller_tomas.reading_groups.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.muller_tomas.reading_groups.dto.CommentRequest;
import com.muller_tomas.reading_groups.dto.CommentResponse;
import com.muller_tomas.reading_groups.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Comments", description = "Endpoints for comment management")
@RestController
public class CommentController {
	private final CommentService commentService;
	
	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}
	@GetMapping("/api/groups/{groupId}/comments")
	@Operation(summary = "Get group comments", description = "Retrieve comments for a group accessible to the authenticated user.")
	@ApiResponse(responseCode = "200", description = "Comments returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User cannot access this group")
	@ApiResponse(responseCode = "404", description = "Group not found")
	public ResponseEntity<Set<CommentResponse>> getGroupComments(@PathVariable int groupId,
			@AuthenticationPrincipal Integer userId) {
		Set<CommentResponse> comments = commentService.getGroupComments(userId, groupId);
		return ResponseEntity.ok(comments);
	}
	@GetMapping("/api/comments/{commentId}")
	@Operation(summary = "Get comment", description = "Retrieve a comment by ID, provided the authenticated user can access its group.")
	@ApiResponse(responseCode = "200", description = "Comment returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User cannot access this comment")
	@ApiResponse(responseCode = "404", description = "Comment not found")
	public ResponseEntity<CommentResponse> getComment(@PathVariable int commentId,
			@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok(commentService.getComment(userId, commentId));
	}
	@DeleteMapping("/api/comments/{commentId}")
	@Operation(summary = "Delete comment", description = "Delete a comment. The service checks that the authenticated user is authorized to delete it.")
	@ApiResponse(responseCode = "204", description = "Comment deleted")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User cannot delete this comment")
	@ApiResponse(responseCode = "404", description = "Comment not found")
	public ResponseEntity<Void> deleteComment(@PathVariable int commentId,
			@AuthenticationPrincipal Integer userId) {
		commentService.deleteComment(userId, commentId);
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/api/groups/{groupId}/comments")
	@Operation(summary = "Add group comment", description = "Create a comment in a group using the supplied comment content and reading-text location information.")
	@ApiResponse(responseCode = "201", description = "Comment created")
	@ApiResponse(responseCode = "400", description = "Invalid comment fields")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	@ApiResponse(responseCode = "403", description = "User cannot access this group")
	@ApiResponse(responseCode = "404", description = "Group not found")
	public ResponseEntity<CommentResponse> addComment(@PathVariable int groupId,
			@RequestBody @Valid CommentRequest commentRequest,
			@AuthenticationPrincipal Integer userId) {
		CommentResponse commentResponse = commentService.addComment(userId, groupId, commentRequest);
		return new ResponseEntity<CommentResponse>(commentResponse, HttpStatus.CREATED);
	}

}
