package com.muller_tomas.reading_groups.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.muller_tomas.reading_groups.TestDataFactory;
import com.muller_tomas.reading_groups.dto.CurrentUserResponse;
import com.muller_tomas.reading_groups.dto.RegisterRequest;
import com.muller_tomas.reading_groups.validation.RegistrationRules;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.DuplicateUserException;
import com.muller_tomas.reading_groups.service.UserService;
import com.muller_tomas.reading_groups.token.JwtTokenFilter;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
	@MockitoBean
	private UserService userService;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtTokenFilter jwtTokenFilter;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void registerUser_shouldReturn200WithTokenPair_whenRequestIsValid() throws Exception {
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();
		TokenPairResponse tokenPairResponse = TestDataFactory.getTokenPairResponse();

		when(userService.createUser(registerRequest)).thenReturn(tokenPairResponse);

		String jsonInput = objectMapper.writeValueAsString(registerRequest);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(jsonInput))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").value(tokenPairResponse.getAccessToken()))
				.andExpect(jsonPath("$.refreshToken").value(tokenPairResponse.getRefreshToken()));
	}

	@Test
	public void registerUser_shouldReturn400_whenUsernameIsBlank() throws Exception {
		String username = "	  ";
		RegisterRequest registerRequest = new RegisterRequest(TestDataFactory.exampleEmail, username,
				TestDataFactory.examplePassword);

		String jsonInput = objectMapper.writeValueAsString(registerRequest);

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(jsonInput))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void registerUser_shouldReturn409_whenUserAlreadyExists() throws Exception {
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();
		String jsonInput = objectMapper.writeValueAsString(registerRequest);
		String errorMessage = "There already exists a user with the specified username";

		doThrow(new DuplicateUserException(errorMessage)).when(userService)
				.createUser(any(RegisterRequest.class));

		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(jsonInput))
				.andExpect(status().isConflict())
				.andDo(result -> {assertEquals(errorMessage, result.getResponse().getContentAsString());});
	}

	@AfterEach
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void currentUser_returnsOnlyAuthenticatedAccountDetails() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(42, null, List.of()));
		Instant availableAt = Instant.parse("2026-10-21T12:00:00Z");
		when(userService.getCurrentUser(42)).thenReturn(
				new CurrentUserResponse(42, "reader", "reader@example.test", false, availableAt));

		mockMvc.perform(get("/api/users/me").param("userId", "99"))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", "no-store"))
				.andExpect(jsonPath("$.userId").value(42))
				.andExpect(jsonPath("$.email").value("reader@example.test"))
				.andExpect(jsonPath("$.emailVerified").value(false))
				.andExpect(jsonPath("$.emailVerificationResendAvailableAt").value(availableAt.toString()))
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.groups").doesNotExist());
		verify(userService).getCurrentUser(42);
	}

	@Test
	public void registrationRulesExposeSharedLimits() throws Exception {
		mockMvc.perform(get("/api/registration-rules"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usernameMinLength").value(RegistrationRules.USERNAME_MIN_LENGTH))
				.andExpect(jsonPath("$.usernameMaxLength").value(RegistrationRules.USERNAME_MAX_LENGTH))
				.andExpect(jsonPath("$.usernamePattern").value(RegistrationRules.USERNAME_PATTERN))
				.andExpect(jsonPath("$.emailMaxLength").value(RegistrationRules.EMAIL_MAX_LENGTH))
				.andExpect(jsonPath("$.emailPattern").value(RegistrationRules.EMAIL_PATTERN))
				.andExpect(jsonPath("$.passwordMinLength").value(10))
				.andExpect(jsonPath("$.passwordMaxUtf8Bytes").value(72));
	}

	@Test
	public void invalidRegistrationFieldsReturnErrorsWithoutCallingService() throws Exception {
		String[][] cases = {
				{ "reader@example.test", "ab", "a".repeat(10), "username" },
				{ "reader@example.test", "a".repeat(26), "a".repeat(10), "username" },
				{ "reader@example.test", "reader name", "a".repeat(10), "username" },
				{ "not-an-email", "reader", "a".repeat(10), "email" },
				{ "reader@example", "reader", "a".repeat(10), "email" },
				{ "reader@example.", "reader", "a".repeat(10), "email" },
				{ "reader@.example", "reader", "a".repeat(10), "email" },
				{ "reader@example..com", "reader", "a".repeat(10), "email" },
				{ "a".repeat(64) + "@" + "b".repeat(31) + ".test", "reader", "a".repeat(10), "email" },
				{ "reader@example.test", "reader", "a".repeat(9), "password" }
		};
		for (String[] values : cases) {
			RegisterRequest request = new RegisterRequest(values[0], values[1], values[2]);
			mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.fieldErrors." + values[3]).isString());
		}
		verify(userService, never()).createUser(any());
	}

	@Test
	public void boundaryLengthsAndSimplePasswordsAreAccepted() throws Exception {
		when(userService.createUser(any())).thenReturn(TestDataFactory.getTokenPairResponse());
		String email = "a".repeat(64) + "@" + "b".repeat(30) + ".test";
		for (String username : List.of("abc", "a".repeat(25), "reader_1.2-3")) {
			RegisterRequest request = new RegisterRequest(email, username, "a".repeat(10));
			mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated());
		}
	}

	@Test
	public void serviceValidationFailureReturnsReadableBadRequest() throws Exception {
		String message = "Password must not exceed 72 UTF-8 bytes";
		when(userService.createUser(any())).thenThrow(new ResponseStatusException(
				HttpStatus.BAD_REQUEST, message));
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(TestDataFactory.getRegisterRequest())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(message));
	}

	@Test
	public void dottedEmailDomainsAreAccepted() throws Exception {
		when(userService.createUser(any())).thenReturn(TestDataFactory.getTokenPairResponse());
		for (String email : List.of("reader@example.com", "reader+books@sub.example.com")) {
			RegisterRequest request = new RegisterRequest(email, "reader", "a".repeat(10));
			mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated());
		}
	}

}
