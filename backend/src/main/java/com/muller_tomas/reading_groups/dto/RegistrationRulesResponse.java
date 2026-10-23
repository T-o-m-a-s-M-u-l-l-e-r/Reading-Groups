package com.muller_tomas.reading_groups.dto;

import com.muller_tomas.reading_groups.validation.RegistrationRules;

public class RegistrationRulesResponse {
	private final int usernameMinLength = RegistrationRules.USERNAME_MIN_LENGTH;
	private final int usernameMaxLength = RegistrationRules.USERNAME_MAX_LENGTH;
	private final String usernamePattern = RegistrationRules.USERNAME_PATTERN;
	private final int emailMaxLength = RegistrationRules.EMAIL_MAX_LENGTH;
	private final int passwordMinLength = RegistrationRules.PASSWORD_MIN_LENGTH;
	private final int passwordMaxUtf8Bytes = RegistrationRules.PASSWORD_MAX_UTF8_BYTES;

	public RegistrationRulesResponse() {
	}

	public int getUsernameMinLength() {
		return usernameMinLength;
	}

	public int getUsernameMaxLength() {
		return usernameMaxLength;
	}

	public String getUsernamePattern() {
		return usernamePattern;
	}

	public int getEmailMaxLength() {
		return emailMaxLength;
	}

	public int getPasswordMinLength() {
		return passwordMinLength;
	}

	public int getPasswordMaxUtf8Bytes() {
		return passwordMaxUtf8Bytes;
	}
}
