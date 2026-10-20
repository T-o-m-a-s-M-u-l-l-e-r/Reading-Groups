package com.muller_tomas.reading_groups.dto;

import java.time.Instant;

public class CreateGroupJoinLinkResponse {
	private String url;
	private Instant expiresAt;

	public CreateGroupJoinLinkResponse() {
	}

	public CreateGroupJoinLinkResponse(String url, Instant expiresAt) {
		this.url = url;
		this.expiresAt = expiresAt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}
}
