package com.muller_tomas.reading_groups.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.muller_tomas.reading_groups.exception.UserNotFoundException;
import com.muller_tomas.reading_groups.model.EmailVerification;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.EmailVerificationRepository;
import com.muller_tomas.reading_groups.repository.UserRepository;

@Service
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final Duration cooldown;
    private final Duration lifetime;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            @Value("${app.email-verification.cooldown:PT2M}") Duration cooldown,
            @Value("${app.email-verification.lifetime:PT24H}") Duration lifetime) {
        if (cooldown.isNegative() || lifetime.isNegative() || lifetime.isZero()) {
            throw new IllegalArgumentException("Invalid email verification durations");
        }
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
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
        EmailVerification verification = verificationRepository.findById(userId).orElse(null);
        if (verification != null && now.isBefore(verification.getRequestedAt().plus(cooldown))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another verification email");
        }

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hashToken(token);
        if (verification == null) {
            verification = new EmailVerification(user, tokenHash, now, now.plus(lifetime));
        } else {
            verification.replaceToken(tokenHash, now, now.plus(lifetime));
        }
        verificationRepository.save(verification);
        String email = user.getEmail();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendVerificationEmail(email, token);
            }
        });
    }

    @Transactional
    public void verify(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw invalidToken();
        }
        String tokenHash = hashToken(token);
        int userId = verificationRepository.findUserIdByTokenHash(tokenHash)
                .orElseThrow(this::invalidToken);
        User user = lockUser(userId);
        // Re-read after locking: a concurrent resend may have replaced the token.
        EmailVerification verification = verificationRepository.findById(userId)
                .orElseThrow(this::invalidToken);
        if (user.isEmailVerified() || !tokenHash.equals(verification.getTokenHash())
                || !verification.getExpiresAt().isAfter(Instant.now())) {
            throw invalidToken();
        }
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);
        verificationRepository.delete(verification);
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
        // TODO: Deliver an email to this address containing the frontend link
        // /verify-email?token=<token>. Do not log or return the raw token.
        // This runs after commit; the future delivery implementation must handle failures/retries.
    }
}
