package com.muller_tomas.reading_groups.model;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum GroupJoinLinkValidity {
	ONE_HOUR(Duration.ofHours(1)),
	ONE_DAY(Duration.ofDays(1)),
	ONE_WEEK(Duration.ofDays(7));

	private final Duration duration;

	private GroupJoinLinkValidity(Duration duration) {
		this.duration = duration;
	}

	@JsonCreator
	public static GroupJoinLinkValidity fromValue(String value) {
		return GroupJoinLinkValidity.valueOf(value);
	}

	public Duration getDuration() {
		return duration;
	}
}
