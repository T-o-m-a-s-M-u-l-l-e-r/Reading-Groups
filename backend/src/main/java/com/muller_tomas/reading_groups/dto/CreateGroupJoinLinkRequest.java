package com.muller_tomas.reading_groups.dto;

import jakarta.validation.constraints.NotNull;

import com.muller_tomas.reading_groups.model.GroupJoinLinkValidity;

public class CreateGroupJoinLinkRequest {
	@NotNull
	private GroupJoinLinkValidity validity;

	public CreateGroupJoinLinkRequest() {
	}

	public CreateGroupJoinLinkRequest(GroupJoinLinkValidity validity) {
		this.validity = validity;
	}

	public GroupJoinLinkValidity getValidity() {
		return validity;
	}

	public void setValidity(GroupJoinLinkValidity validity) {
		this.validity = validity;
	}
}
