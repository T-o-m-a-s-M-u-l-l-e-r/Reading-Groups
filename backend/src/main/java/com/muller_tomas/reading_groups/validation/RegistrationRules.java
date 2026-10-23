package com.muller_tomas.reading_groups.validation;

public final class RegistrationRules {
	public static final int USERNAME_MIN_LENGTH = 3;
	public static final int USERNAME_MAX_LENGTH = 25;
	public static final String USERNAME_PATTERN = "^[A-Za-z0-9_.-]+$";
	public static final int EMAIL_MAX_LENGTH = 100;
	public static final int PASSWORD_MIN_LENGTH = 10;
	public static final int PASSWORD_MAX_UTF8_BYTES = 72;

	private RegistrationRules() {
	}
}
