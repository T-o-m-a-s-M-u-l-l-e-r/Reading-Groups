package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import com.muller_tomas.reading_groups.repository.RefreshSessionRepository;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.muller_tomas.reading_groups.TestDataFactory;
import com.muller_tomas.reading_groups.dto.CurrentUserResponse;
import com.muller_tomas.reading_groups.dto.RegisterRequest;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.DuplicateUserException;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.UserRepository;
import com.muller_tomas.reading_groups.token.JwtTokenProvider;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	private UserService userService;
	@Mock
	private EmailVerificationService emailVerificationService;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@Mock
	private RefreshSessionRepository refreshSessionRepository;

	@BeforeEach
	public void setUp() {
		userService = new UserService(userRepository, passwordEncoder, jwtTokenProvider, refreshSessionRepository,
				emailVerificationService);
	}

	@Test
	public void createUser_shouldThrowDuplicateUserException_whenUsernameExists() {
		User duplicatedUser = TestDataFactory.getSavedUser();
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();

		when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(duplicatedUser));
		assertThrows(DuplicateUserException.class, () -> userService.createUser(registerRequest));
	}

	@Test
	public void createUser_shouldThrowDuplicateUserException_whenEmailExists() {
			User duplicatedUser = TestDataFactory.getSavedUser();
			RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();

			when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(duplicatedUser));
			assertThrows(DuplicateUserException.class, () -> userService.createUser(registerRequest));
		}

	@Test
	public void createUser_shouldReturnTokenPairResponse_whenRequestIsValid() {
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();
		registerRequest.setPassword("é".repeat(36)); // Exactly 72 UTF-8 bytes.
		String examplePaswordHash = TestDataFactory.examplePasswordHash;

		when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
		when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(examplePaswordHash);

		User createdUser = TestDataFactory.getCreatedUser();
		User savedUser = TestDataFactory.getSavedUser();
		when(userRepository.save(createdUser)).thenReturn(savedUser);
		
		String exampleAccessToken = TestDataFactory.exampleAccessToken;
		String exampleRefreshToken = TestDataFactory.exampleRefreshToken;

		when(jwtTokenProvider.createToken(savedUser.getId(), TokenType.ACCESS)).thenReturn(exampleAccessToken);
		when(jwtTokenProvider.createToken(savedUser.getId(), TokenType.REFRESH)).thenReturn(exampleRefreshToken);

		TokenPairResponse tokenPairResponse = userService.createUser(registerRequest);
		assertEquals(TestDataFactory.getTokenPairResponse(), tokenPairResponse);
		verify(emailVerificationService).requestVerification(savedUser.getId());
		
		verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
		verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
		verify(userRepository, times(1)).save(createdUser);
	}

	@Test
	public void currentUser_reportsUnverifiedStatusAndResendAvailability() {
		User user = TestDataFactory.getSavedUser();
		Instant availableAt = Instant.parse("2026-10-21T12:00:00Z");
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(emailVerificationService.getResendAvailableAt(user.getId())).thenReturn(availableAt);

		CurrentUserResponse response = userService.getCurrentUser(user.getId());
		assertEquals(user.getId(), response.getUserId());
		assertEquals(user.getEmail(), response.getEmail());
		assertFalse(response.isEmailVerified());
		assertEquals(availableAt, response.getEmailVerificationResendAvailableAt());
	}

	@Test
	public void currentUser_verifiedAccountHasNoResendAvailability() {
		User user = TestDataFactory.getSavedUser();
		user.setEmailVerifiedAt(Instant.now());
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		CurrentUserResponse response = userService.getCurrentUser(user.getId());
		assertTrue(response.isEmailVerified());
		assertNull(response.getEmailVerificationResendAvailableAt());
		verify(emailVerificationService, never()).getResendAvailableAt(user.getId());
	}

	@Test
	public void passwordsAboveUtf8ByteLimitAreRejectedBeforeDatabaseOrHashing() {
		for (String password : List.of("a".repeat(73), "é".repeat(37))) {
			RegisterRequest request = TestDataFactory.getRegisterRequest();
			request.setPassword(password);
			ResponseStatusException exception = assertThrows(ResponseStatusException.class,
					() -> userService.createUser(request));
			assertEquals(400, exception.getStatusCode().value());
		}
		verifyNoInteractions(userRepository, passwordEncoder, emailVerificationService, refreshSessionRepository);
	}

}
