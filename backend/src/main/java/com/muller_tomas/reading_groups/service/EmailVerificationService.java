package com.muller_tomas.reading_groups.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.muller_tomas.reading_groups.exception.EmailDeliveryException;
import com.muller_tomas.reading_groups.exception.UserNotFoundException;
import com.muller_tomas.reading_groups.model.EmailVerification;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.EmailVerificationRepository;
import com.muller_tomas.reading_groups.repository.UserRepository;

@Service
public class EmailVerificationService {
	private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

	private final EmailService emailService;
	private final String verificationPageUrl;
	private final UserRepository userRepository;
	private final EmailVerificationRepository emailVerificationRepository;
	private final Duration cooldown;
	private final Duration lifetime;
	private final SecureRandom secureRandom = new SecureRandom();

	public EmailVerificationService(UserRepository userRepository,
			EmailVerificationRepository emailVerificationRepository, EmailService emailService,
			@Value("${app.email-verification.page-url}") String verificationPageUrl,
			@Value("${app.email-verification.cooldown:PT2M}") Duration cooldown,
			@Value("${app.email-verification.lifetime:PT24H}") Duration lifetime) {
		if (cooldown.isNegative() || lifetime.isNegative() || lifetime.isZero()) {
			throw new IllegalArgumentException("Invalid email verification durations");
		}
		this.userRepository = userRepository;
		this.emailVerificationRepository = emailVerificationRepository;
		this.emailService = emailService;
		this.verificationPageUrl = verificationPageUrl;
		this.cooldown = cooldown;
		this.lifetime = lifetime;
	}

	@Transactional
	public void requestVerification(int userId) {
		User user = lockUser(userId);
		if (user.isEmailVerified()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified");
		}

		Instant now = Instant.now();
		EmailVerification emailVerification = emailVerificationRepository.findById(userId).orElse(null);
		if (emailVerification != null && now.isBefore(emailVerification.getRequestedAt().plus(cooldown))) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Please wait before requesting another verification email");
		}

		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

		String tokenHash = hashToken(token);
		if (emailVerification == null) {
			emailVerification = new EmailVerification(user, tokenHash, now, now.plus(lifetime));
		} else {
			emailVerification.replaceToken(tokenHash, now, now.plus(lifetime));
		}

		emailVerificationRepository.save(emailVerification);

		String email = user.getEmail();
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					sendVerificationEmail(email, token);
				} catch (EmailDeliveryException exception) {
					// The account and token are already committed; the user can request a resend.
					// Avoid logging the exception, which may contain the message or credentials.
					logger.warn("Verification email delivery failed for user {}", userId);
				}
			}
		});
	}

	@Transactional(readOnly = true)
	public Instant getResendAvailableAt(int userId) {
		return emailVerificationRepository.findById(userId)
				.map(verification -> verification.getRequestedAt().plus(cooldown))
				.orElse(null);
	}

	@Transactional
	public void verify(String token) {
		if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
			throw invalidToken();
		}

		String tokenHash = hashToken(token);
		int userId = emailVerificationRepository.findUserIdByTokenHash(tokenHash)
				.orElseThrow(() -> invalidToken());
		User user = lockUser(userId);

		// Re-read after locking: a concurrent resend may have replaced the token.
		EmailVerification emailVerification = emailVerificationRepository.findById(userId)
				.orElseThrow(() -> invalidToken());
		if (user.isEmailVerified() || !tokenHash.equals(emailVerification.getTokenHash())
				|| !emailVerification.getExpiresAt().isAfter(Instant.now())) {
			throw invalidToken();
		}

		user.setEmailVerifiedAt(Instant.now());
		userRepository.save(user);
		emailVerificationRepository.delete(emailVerification);
	}

	private User lockUser(int userId) {
		return userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new UserNotFoundException("User with specified id not found"));
	}

	private ResponseStatusException invalidToken() {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid or expired");
	}

	private String hashToken(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private void sendVerificationEmail(String email, String token) {
		String verificationLink = UriComponentsBuilder.fromUriString(verificationPageUrl)
				.queryParam("token", token)
				.build()
				.encode()
				.toUriString();
		String subject = "Verify your Reading Groups email address";
		String body = "Welcome to Reading Groups!\n\n"
				+ "Confirm your email address by opening this link:\n"
				+ verificationLink + "\n\n"
				+ "This link expires in " + lifetime.toHours() + " hours.\n"
				+ "If you did not create this account, you can ignore this email.";

		emailService.sendEmail(email, subject, body);
	}
}
