package com.muller_tomas.reading_groups.dto;

public class GroupJoinPreviewResponse {
	private String groupName;
	private String administratorName;
	private int memberCount;
	private boolean hasIcon;

	public GroupJoinPreviewResponse() {
	}

	public GroupJoinPreviewResponse(String groupName, String administratorName,
			int memberCount, boolean hasIcon) {
		this.groupName = groupName;
		this.administratorName = administratorName;
		this.memberCount = memberCount;
		this.hasIcon = hasIcon;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getAdministratorName() {
		return administratorName;
	}

	public void setAdministratorName(String administratorName) {
		this.administratorName = administratorName;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public boolean getHasIcon() {
		return hasIcon;
	}

	public void setHasIcon(boolean hasIcon) {
		this.hasIcon = hasIcon;
	}
}
