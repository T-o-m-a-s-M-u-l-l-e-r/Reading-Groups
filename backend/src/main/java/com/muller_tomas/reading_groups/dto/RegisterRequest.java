package com.muller_tomas.reading_groups.dto;

import java.util.Objects;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.muller_tomas.reading_groups.validation.RegistrationRules;

public class RegisterRequest {

	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Email must be valid")
	@Size(max = RegistrationRules.EMAIL_MAX_LENGTH, message = "Email must not exceed {max} characters")
	private String email;

	@NotBlank(message = "Username cannot be empty")
	@Size(min = RegistrationRules.USERNAME_MIN_LENGTH, max = RegistrationRules.USERNAME_MAX_LENGTH,
			message = "Username must be between {min} and {max} characters")
	@Pattern(regexp = RegistrationRules.USERNAME_PATTERN,
			message = "Username may contain only A-Z, a-z, digits, dots, underscores, and hyphens")
	private String username;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = RegistrationRules.PASSWORD_MIN_LENGTH, message = "Password must be at least {min} characters")
	private String password;

	public RegisterRequest(String email, String username, String password) {
		this.email = email;
		this.username = username;
		this.password = password;
	}

	public RegisterRequest() {
		super();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof RegisterRequest)) {
			return false;
		}

		RegisterRequest req = (RegisterRequest) obj;

		return password.equals(req.getPassword()) && username.equals(req.getUsername()) && email.equals(req.getEmail());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(password, username, email);
	}

}
