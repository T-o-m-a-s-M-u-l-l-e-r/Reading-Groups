package com.muller_tomas.reading_groups.dto;

import java.time.Instant;

public class GroupJoinLinkResponse {
	private Instant issuedAt;
	private Instant expiresAt;
	private boolean expired;

	public GroupJoinLinkResponse() {
	}

	public GroupJoinLinkResponse(Instant issuedAt, Instant expiresAt, boolean expired) {
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.expired = expired;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Instant issuedAt) {
		this.issuedAt = issuedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}
}
