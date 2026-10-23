package com.muller_tomas.reading_groups.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import com.muller_tomas.reading_groups.model.RefreshSession;
import com.muller_tomas.reading_groups.repository.RefreshSessionRepository;

import com.muller_tomas.reading_groups.dto.CurrentUserResponse;
import com.muller_tomas.reading_groups.dto.RegisterRequest;
import com.muller_tomas.reading_groups.validation.RegistrationRules;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.DuplicateUserException;
import com.muller_tomas.reading_groups.exception.UserNotFoundException;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.UserRepository;
import com.muller_tomas.reading_groups.token.JwtTokenProvider;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

@Service
public class UserService {
	private final EmailVerificationService emailVerificationService;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final RefreshSessionRepository refreshSessionRepository;

	public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider, RefreshSessionRepository refreshSessionRepository,
			EmailVerificationService emailVerificationService) {
		this.emailVerificationService = emailVerificationService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshSessionRepository = refreshSessionRepository;
	}

	@Transactional
	public TokenPairResponse createUser(RegisterRequest registerRequest) throws DuplicateUserException {
		if (registerRequest.getPassword().getBytes(StandardCharsets.UTF_8).length
				> RegistrationRules.PASSWORD_MAX_UTF8_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Password must not exceed " + RegistrationRules.PASSWORD_MAX_UTF8_BYTES + " UTF-8 bytes");
		}

		if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
			throw new DuplicateUserException("There already exists a user with the specified username");
		}

		if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
			throw new DuplicateUserException("There already exists a user with the specified email");
		}

		User user = new User();
		user.setEmail(registerRequest.getEmail());
		user.setUsername(registerRequest.getUsername());

		String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
		user.setPasswordHash(hashedPassword);

		User createdUser = userRepository.save(user);
		int userId = createdUser.getId();
		emailVerificationService.requestVerification(userId);

		String refreshToken = jwtTokenProvider.createToken(userId, TokenType.REFRESH);
		String accessToken = jwtTokenProvider.createToken(userId, TokenType.ACCESS);

		refreshSessionRepository.save(new RefreshSession(createdUser, jwtTokenProvider.hashToken(refreshToken),
				jwtTokenProvider.getExpiration(refreshToken)));
		return new TokenPairResponse(accessToken, refreshToken);
	}

	@Transactional(readOnly = true)
	public CurrentUserResponse getCurrentUser(int userId) {
		User user = findUserByUserId(userId);
		return new CurrentUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isEmailVerified(),
				user.isEmailVerified() ? null : emailVerificationService.getResendAvailableAt(userId));
	}

	public User findUserByUsernameOrEmail(String login) throws UsernameNotFoundException {
		return userRepository.findByUsername(login).or(() -> userRepository.findByEmail(login))
				.orElseThrow(() -> new UserNotFoundException("User with specified login not found"));
	}

	public User findUserByUserId(int userId) {
		return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with specified id not found"));
	}

}
