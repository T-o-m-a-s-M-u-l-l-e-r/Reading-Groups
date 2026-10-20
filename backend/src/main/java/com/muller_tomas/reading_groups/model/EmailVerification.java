package com.muller_tomas.reading_groups.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {
	@Id
	@Column(name = "user_id")
	private Integer userId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	public EmailVerification() {
	}

	public EmailVerification(User user, String tokenHash, Instant requestedAt, Instant expiresAt) {
		this.user = user;
		replaceToken(tokenHash, requestedAt, expiresAt);
	}

	public void replaceToken(String tokenHash, Instant requestedAt, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.requestedAt = requestedAt;
		this.expiresAt = expiresAt;
	}

	public Integer getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
