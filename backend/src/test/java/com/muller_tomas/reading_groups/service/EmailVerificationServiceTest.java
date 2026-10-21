package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.muller_tomas.reading_groups.model.EmailVerification;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.EmailVerificationRepository;
import com.muller_tomas.reading_groups.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private EmailVerificationRepository emailVerificationRepository;

	@Mock
	private EmailService emailService;

	private EmailVerificationService emailVerificationService;
	private User user;
	private final String token = "a".repeat(43);

	@BeforeEach
	void setup() {
		emailVerificationService = new EmailVerificationService(userRepository, emailVerificationRepository,
				emailService, "https://app.example/verify-email",
				Duration.ofMinutes(2), Duration.ofHours(24));
		user = new User();
		user.setId(1);
	}

	String hash(String value) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(value.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void issuesHashedTokenAndDefersDeliveryUntilCommit() {
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		TransactionSynchronizationManager.initSynchronization();
		try {
			emailVerificationService.requestVerification(1);
			ArgumentCaptor<EmailVerification> verificationCaptor = ArgumentCaptor.forClass(EmailVerification.class);
			verify(emailVerificationRepository).save(verificationCaptor.capture());
			EmailVerification savedVerification = verificationCaptor.getValue();
			assertSame(user, savedVerification.getUser());
			assertTrue(savedVerification.getTokenHash().matches("[0-9a-f]{64}"));
			assertEquals(Duration.ofHours(24),
					Duration.between(savedVerification.getRequestedAt(), savedVerification.getExpiresAt()));
			assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void rejectsResendDuringCooldown() {
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		when(emailVerificationRepository.findById(1)).thenReturn(Optional.of(
				new EmailVerification(user, "old", Instant.now(), Instant.now().plusSeconds(3600))));
		assertEquals(HttpStatus.TOO_MANY_REQUESTS, assertThrows(ResponseStatusException.class,
				() -> emailVerificationService.requestVerification(1)).getStatusCode());
		verify(emailVerificationRepository, never()).save(any());
	}

	@Test
	void resendReplacesPreviousTokenAfterCooldown() {
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		EmailVerification existingVerification = new EmailVerification(user, "old", Instant.now().minusSeconds(180),
				Instant.now().plusSeconds(3600));
		when(emailVerificationRepository.findById(1)).thenReturn(Optional.of(existingVerification));
		TransactionSynchronizationManager.initSynchronization();
		try {
			emailVerificationService.requestVerification(1);
			assertNotEquals("old", existingVerification.getTokenHash());
			verify(emailVerificationRepository).save(existingVerification);
		} finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	void rejectsResendForVerifiedUser() {
		user.setEmailVerifiedAt(Instant.now());
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
				() -> emailVerificationService.requestVerification(1)).getStatusCode());
		verifyNoInteractions(emailVerificationRepository);
	}

	@Test
	void verifiesAssociatedUserAndConsumesToken() throws Exception {
		when(emailVerificationRepository.findUserIdByTokenHash(hash(token))).thenReturn(Optional.of(1));
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		EmailVerification existingVerification = new EmailVerification(user, hash(token), Instant.now(),
				Instant.now().plusSeconds(60));
		when(emailVerificationRepository.findById(1)).thenReturn(Optional.of(existingVerification));
		emailVerificationService.verify(token);
		assertTrue(user.isEmailVerified());
		verify(userRepository).save(user);
		verify(emailVerificationRepository).delete(existingVerification);
	}

	@Test
	void rejectsExpiredToken() throws Exception {
		assertInvalidStoredToken(hash(token), Instant.now().minusSeconds(1));
	}

	@Test
	void rejectsTokenReplacedByConcurrentResend() throws Exception {
		assertInvalidStoredToken("replacement", Instant.now().plusSeconds(60));
	}

	void assertInvalidStoredToken(String storedHash, Instant expiresAt) throws Exception {
		when(emailVerificationRepository.findUserIdByTokenHash(hash(token))).thenReturn(Optional.of(1));
		when(userRepository.findByIdForUpdate(1)).thenReturn(Optional.of(user));
		when(emailVerificationRepository.findById(1)).thenReturn(Optional.of(
				new EmailVerification(user, storedHash, Instant.now().minusSeconds(120), expiresAt)));
		assertThrows(ResponseStatusException.class, () -> emailVerificationService.verify(token));
		assertFalse(user.isEmailVerified());
		verify(emailVerificationRepository, never()).delete(any());
	}

	@Test
	void rejectsUnknownToken() {
		assertThrows(ResponseStatusException.class, () -> emailVerificationService.verify(token));
		verifyNoInteractions(userRepository);
	}

	@Test
	void rejectsMalformedTokenWithoutDatabaseLookup() {
		assertThrows(ResponseStatusException.class, () -> emailVerificationService.verify("invalid"));
		verifyNoInteractions(userRepository, emailVerificationRepository);
	}
	@Test
	void resendAvailabilityUsesConfiguredCooldown() {
		Instant requestedAt = Instant.parse("2026-10-21T12:00:00Z");
		EmailVerification verification = new EmailVerification(user, "hash", requestedAt,
				requestedAt.plus(Duration.ofHours(24)));
		when(emailVerificationRepository.findById(1)).thenReturn(Optional.of(verification));

		assertEquals(requestedAt.plus(Duration.ofMinutes(2)), emailVerificationService.getResendAvailableAt(1));
	}

}
