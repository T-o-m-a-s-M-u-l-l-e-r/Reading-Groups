package com.muller_tomas.reading_groups.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private final SecretKey signingKey;
	private static final String tokenTypeClaim = "token_type";

	public enum TokenType {
		ACCESS, REFRESH;

		public Duration getValidityDuration() {

			return switch (this) {
			case ACCESS -> Duration.ofMinutes(15);
			case REFRESH -> Duration.ofDays(7);
			};

		}

	}

	public JwtTokenProvider(@Value("${jwt.secret}") String secret) {

		try {
			byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
			signingKey = Keys.hmacShaKeyFor(keyBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}

	}

	public String createToken(int userId, TokenType tokenType) {
		Date now = new Date();
		Date validity = new Date(now.getTime() + tokenType.getValidityDuration().toMillis());

		return Jwts.builder().id(UUID.randomUUID().toString()).claim(tokenTypeClaim, tokenType.toString()).subject(String.valueOf(userId)).issuedAt(now)
				.expiration(validity).signWith(signingKey).compact();
	}

	public TokenType getTokenType(String token) throws IllegalArgumentException {
		String tokenType = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload()
				.get(tokenTypeClaim, String.class);
		return TokenType.valueOf(tokenType);
	}

	public int getUserId(String token) {
		Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
		return Integer.valueOf(claims.getSubject());
	}

    public Instant getExpiration(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)
                .getPayload().getExpiration().toInstant();
    }

    public String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

	public boolean isTokenValid(String token) {

		try {
			Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}

	}

}
