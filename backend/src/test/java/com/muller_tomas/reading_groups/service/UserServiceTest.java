package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import com.muller_tomas.reading_groups.repository.RefreshSessionRepository;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.muller_tomas.reading_groups.TestDataFactory;
import com.muller_tomas.reading_groups.dto.RegisterRequest;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.DuplicateUserException;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.UserRepository;
import com.muller_tomas.reading_groups.token.JwtTokenProvider;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	private UserService userService;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@Mock
	private RefreshSessionRepository refreshSessionRepository;

	@BeforeEach
	public void setUp() {
		userService = new UserService(userRepository, passwordEncoder, jwtTokenProvider, refreshSessionRepository);
	}

	@Test
	public void createUser_shouldThrowDuplicateUserException_whenUsernameExists() {
		User duplicatedUser = TestDataFactory.getSavedUser();
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();

		when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(duplicatedUser));
		assertThrows(DuplicateUserException.class, () -> userService.createUser(registerRequest));
	}

	@Test
	public void createUser_shouldThrowDuplicateUserException_whenEmailExists() {
			User duplicatedUser = TestDataFactory.getSavedUser();
			RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();

			when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(duplicatedUser));
			assertThrows(DuplicateUserException.class, () -> userService.createUser(registerRequest));
		}

	@Test
	public void createUser_shouldReturnTokenPairResponse_whenRequestIsValid() {
		RegisterRequest registerRequest = TestDataFactory.getRegisterRequest();
		String examplePaswordHash = TestDataFactory.examplePasswordHash;

		when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
		when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(examplePaswordHash);

		User createdUser = TestDataFactory.getCreatedUser();
		User savedUser = TestDataFactory.getSavedUser();
		when(userRepository.save(createdUser)).thenReturn(savedUser);
		
		String exampleAccessToken = TestDataFactory.exampleAccessToken;
		String exampleRefreshToken = TestDataFactory.exampleRefreshToken;

		when(jwtTokenProvider.createToken(savedUser.getId(), TokenType.ACCESS)).thenReturn(exampleAccessToken);
		when(jwtTokenProvider.createToken(savedUser.getId(), TokenType.REFRESH)).thenReturn(exampleRefreshToken);

		TokenPairResponse tokenPairResponse = userService.createUser(registerRequest);
		assertEquals(TestDataFactory.getTokenPairResponse(), tokenPairResponse);
		
		verify(userRepository, times(1)).findByUsername(registerRequest.getUsername());
		verify(userRepository, times(1)).findByEmail(registerRequest.getEmail());
		verify(userRepository, times(1)).save(createdUser);
	}

}
