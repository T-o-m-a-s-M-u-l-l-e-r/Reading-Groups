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
@Table(name = "group_join_links")
public class GroupJoinLink {
	@Id
	@Column(name = "group_id")
	private Integer groupId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id")
	private Group group;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "issued_at", nullable = false)
	private Instant issuedAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	public GroupJoinLink() {
	}

	public GroupJoinLink(Group group, String tokenHash, Instant issuedAt, Instant expiresAt) {
		this.group = group;
		replaceToken(tokenHash, issuedAt, expiresAt);
	}

	public void replaceToken(String tokenHash, Instant issuedAt, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public Group getGroup() {
		return group;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
