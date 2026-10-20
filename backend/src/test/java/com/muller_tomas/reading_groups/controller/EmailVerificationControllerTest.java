package com.muller_tomas.reading_groups.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.muller_tomas.reading_groups.service.EmailVerificationService;
import com.muller_tomas.reading_groups.token.JwtTokenFilter;

@WebMvcTest(EmailVerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailVerificationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmailVerificationService emailVerificationService;

	@MockitoBean
	private JwtTokenFilter jwtTokenFilter;

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void resendUsesAuthenticatedUserWithoutRequestBody() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(42, null, List.of()));
		mockMvc.perform(post("/api/auth/email-verification/resend")).andExpect(status().isNoContent());
		verify(emailVerificationService).requestVerification(42);
	}

	@Test
	void verificationAcceptsTokenWithoutLogin() throws Exception {
		mockMvc.perform(post("/api/auth/email-verification/verify")
				.contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"emailed-token\"}"))
				.andExpect(status().isNoContent());
		verify(emailVerificationService).verify("emailed-token");
	}

	@Test
	void verificationRejectsBlankToken() throws Exception {
		mockMvc.perform(post("/api/auth/email-verification/verify")
				.contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"\"}"))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(emailVerificationService);
	}
}
