package com.muller_tomas.reading_groups.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.muller_tomas.reading_groups.dto.CurrentUserResponse;
import com.muller_tomas.reading_groups.dto.RegisterRequest;
import com.muller_tomas.reading_groups.dto.RegistrationRulesResponse;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Users", description = "Endpoints for user operations")
@RestController
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/api/registration-rules")
	@Operation(summary = "Get registration rules",
			description = "Public input limits. Character lengths use UTF-16 units; the password byte limit uses UTF-8.")
	public ResponseEntity<RegistrationRulesResponse> getRegistrationRules() {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new RegistrationRulesResponse());
	}

	@GetMapping("/api/users/me")
	@Operation(summary = "Get my account",
			description = "Return the authenticated user's identity and email verification status.")
	@ApiResponse(responseCode = "200", description = "Current account returned")
	@ApiResponse(responseCode = "401", description = "Authentication required")
	public ResponseEntity<CurrentUserResponse> getCurrentUser(@AuthenticationPrincipal Integer userId) {
		return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(userService.getCurrentUser(userId));
	}

	@PostMapping("/api/users")
	@Operation(summary = "Register user", description = "Create an account using a username, email address and password; return access and refresh tokens.")
	@ApiResponse(responseCode = "201", description = "Account created; token pair returned")
	@ApiResponse(responseCode = "400", description = "Invalid registration fields")
	@ApiResponse(responseCode = "409", description = "Username or email address already in use")
	public ResponseEntity<TokenPairResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		TokenPairResponse tokenPairResponse = userService.createUser(registerRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(tokenPairResponse);
	}

}
