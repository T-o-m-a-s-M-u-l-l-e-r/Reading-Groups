package com.muller_tomas.reading_groups.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.muller_tomas.reading_groups.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationException e, HttpServletRequest request) {
		ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}
	
	@ExceptionHandler(GroupNotFoundException.class)
	public ResponseEntity<?> handleGroupNotFound(GroupNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
	
	@ExceptionHandler(InviteNotFoundException.class)
	public ResponseEntity<?> handleInviteNotFound(InviteNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
	
	@ExceptionHandler(CommentNotFoundException.class)
	public ResponseEntity<?> handleCommentNotFound(CommentNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}

	@ExceptionHandler(TokenRevokedException.class)
	public ResponseEntity<?> handleTokenRevoked(TokenRevokedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
	}

	@ExceptionHandler(DuplicateUserException.class)
	public ResponseEntity<?> handleDuplicateUser(DuplicateUserException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}

	@ExceptionHandler(InvalidTokenTypeException.class)
	public ResponseEntity<?> handleInvalidTokenType(InvalidTokenTypeException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	}
	
	@ExceptionHandler(MissingAuthorizationHeader.class)
	public ResponseEntity<?> handleMissingAuthHeader(MissingAuthorizationHeader e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}
	
	@ExceptionHandler(InvalidGroupIconException.class)
	public ResponseEntity<String> handleInvalidGroupIcon(InvalidGroupIconException exception) {
		return ResponseEntity.badRequest().body(exception.getMessage());
	}

	@ExceptionHandler(InvalidReadingTextException.class)
	public ResponseEntity<?> handleInvalidReadingText(InvalidReadingTextException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}
	
	@ExceptionHandler(FileStorageException.class)
	public ResponseEntity<?> handleFileStorageException(FileStorageException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	}
	
	@ExceptionHandler(StoredFileNotFoundException.class)
	public ResponseEntity<?> handleStoredFileNotFound(StoredFileNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
	
	@ExceptionHandler(InvalidResourceIdException.class)
	public ResponseEntity<?> handleInvalidResourceIdException(InvalidResourceIdException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
	    String errorMessage = e.getBindingResult()
	        .getFieldErrors()
	        .stream()
	        .map(error -> error.getField() + ": " + error.getDefaultMessage())
	        .collect(Collectors.joining(", "));
	    
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
	}

}