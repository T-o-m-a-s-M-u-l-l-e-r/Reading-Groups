package com.muller_tomas.reading_groups.dto;

import java.time.Instant;

public class CurrentUserResponse {
	private int userId;
	private String username;
	private String email;
	private boolean emailVerified;
	private Instant emailVerificationResendAvailableAt;

	public CurrentUserResponse() {
	}

	public CurrentUserResponse(int userId, String username, String email, boolean emailVerified,
			Instant emailVerificationResendAvailableAt) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.emailVerified = emailVerified;
		this.emailVerificationResendAvailableAt = emailVerificationResendAvailableAt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Instant getEmailVerificationResendAvailableAt() {
		return emailVerificationResendAvailableAt;
	}

	public void setEmailVerificationResendAvailableAt(Instant emailVerificationResendAvailableAt) {
		this.emailVerificationResendAvailableAt = emailVerificationResendAvailableAt;
	}
}
