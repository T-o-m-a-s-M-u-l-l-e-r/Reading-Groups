package com.muller_tomas.reading_groups.dto;

import java.time.LocalDateTime;

public class GroupResponse {
	private boolean hasIcon;
	private int id;
	private int administratorUserId;
	private LocalDateTime createdAt;
	private String groupName;
	private int memberCount;
	private String administratorName;
	
	public GroupResponse(int id, int administratorUserId, LocalDateTime createdAt, String groupName, int memberCount, String administratorName) {
		super();
		this.id = id;
		this.administratorUserId = administratorUserId;
		this.createdAt = createdAt;
		this.groupName = groupName;
		this.memberCount = memberCount;
		this.administratorName = administratorName;
	}
	
	public GroupResponse() {
		
	}

	public boolean getHasIcon() {
		return hasIcon;
	}

	public void setHasIcon(boolean hasIcon) {
		this.hasIcon = hasIcon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAdministratorUserId() {
		return administratorUserId;
	}

	public void setAdministratorUserId(int administratorUserId) {
		this.administratorUserId = administratorUserId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public String getAdministratorName() {
		return administratorName;
	}

	public void setAdministratorName(String administratorName) {
		this.administratorName = administratorName;
	}

}
