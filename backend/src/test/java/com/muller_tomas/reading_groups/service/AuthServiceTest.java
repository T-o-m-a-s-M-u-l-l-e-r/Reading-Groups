package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.muller_tomas.reading_groups.TestDataFactory;
import com.muller_tomas.reading_groups.dto.LoginRequest;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.InvalidTokenTypeException;
import com.muller_tomas.reading_groups.exception.TokenRevokedException;
import com.muller_tomas.reading_groups.model.RefreshSession;
import com.muller_tomas.reading_groups.repository.RefreshSessionRepository;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.token.JwtTokenProvider;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
	@Mock
	private JwtTokenProvider tokenProvider;
	
	@Mock
	private UserService userService;
	
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	
	private AuthService authService;
	
	@Mock
	private RefreshSessionRepository refreshSessionRepository;

	@BeforeEach
	public void setUp() {
		authService = new AuthService(userService, passwordEncoder, tokenProvider, refreshSessionRepository);
	}
	
	@Test
	public void loginUser_shouldReturnTokenPairResponse_whenRequestIsValid() {
		LoginRequest loginRequest = new LoginRequest(TestDataFactory.exampleUsername, TestDataFactory.examplePassword);
		User loginUser = TestDataFactory.getSavedUser();
		 
		when(userService.findUserByUsernameOrEmail(loginRequest.getLogin())).thenReturn(loginUser);
		when(passwordEncoder.matches(loginRequest.getPassword(), loginUser.getPasswordHash())).thenReturn(true);
		when(tokenProvider.createToken(loginUser.getId(), TokenType.ACCESS)).thenReturn(TestDataFactory.exampleAccessToken);
		when(tokenProvider.createToken(loginUser.getId(), TokenType.REFRESH)).thenReturn(TestDataFactory.exampleRefreshToken);
		
		assertEquals(TestDataFactory.getTokenPairResponse(), authService.loginUser(loginRequest));
	}
	
	@Test
	public void loginUser_shouldThrowBadCredentialsException_whenPasswordIsInvalid() {
		String wrongPassword = TestDataFactory.examplePassword + "d";
		LoginRequest loginRequest = new LoginRequest(TestDataFactory.exampleUsername, wrongPassword);
		User savedUser = TestDataFactory.getSavedUser();
		 
		when(userService.findUserByUsernameOrEmail(loginRequest.getLogin())).thenReturn(savedUser);
		when(passwordEncoder.matches(loginRequest.getPassword(), savedUser.getPasswordHash())).thenReturn(false);
		
		assertThrows(BadCredentialsException.class, () -> {authService.loginUser(loginRequest);});
	}


    @Test
    void loginPersistsHashedRefreshTokenWithExactExpiry() {
        JwtTokenProvider realProvider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, realProvider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        LoginRequest request = new LoginRequest(TestDataFactory.exampleUsername, TestDataFactory.examplePassword);
        when(userService.findUserByUsernameOrEmail(request.getLogin())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        TokenPairResponse response = service.loginUser(request);
        ArgumentCaptor<RefreshSession> captor =
                ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessionRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        assertEquals(realProvider.hashToken(response.getRefreshToken()), captor.getValue().getTokenHash());
        assertEquals(realProvider.getExpiration(response.getRefreshToken()), captor.getValue().getExpiresAt());
    }

    @Test
    void refreshRejectsMissingSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> service.refreshToken(token));
    }

    @Test
    void refreshReturnsAccessTokenForActiveSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        var session = new RefreshSession(user, hash, provider.getExpiration(token));
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.of(session));

        String access = service.refreshToken(token);

        assertEquals(TokenType.ACCESS, provider.getTokenType(access));
        assertEquals(user.getId(), provider.getUserId(access));
    }

    @Test
    void refreshRejectsExpiredSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        var session = new RefreshSession(user, hash, provider.getExpiration(token));
        session.setExpiresAt(Instant.now().minusSeconds(1));
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.of(session));

        assertThrows(BadCredentialsException.class, () -> service.refreshToken(token));
    }

    @Test
    void logoutRevokesSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        var session = new RefreshSession(user, hash, provider.getExpiration(token));
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.of(session));

        service.logoutUser(token);

        assertNotNull(session.getRevokedAt());
        verify(refreshSessionRepository).save(session);
    }

    @Test
    void refreshRejectsRevokedSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        var session = new RefreshSession(user, hash, provider.getExpiration(token));
        session.setRevokedAt(Instant.now());
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.of(session));

        assertThrows(TokenRevokedException.class, () -> service.refreshToken(token));
    }

    @Test
    void logoutRejectsRevokedSession() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        User user = TestDataFactory.getSavedUser();
        String token = provider.createToken(user.getId(), TokenType.REFRESH);
        String hash = provider.hashToken(token);
        var session = new RefreshSession(user, hash, provider.getExpiration(token));
        session.setRevokedAt(Instant.now());
        when(refreshSessionRepository.findByTokenHash(hash)).thenReturn(Optional.of(session));

        assertThrows(TokenRevokedException.class, () -> service.logoutUser(token));
    }

    @Test
    void refreshRejectsAccessToken() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);
        String access = provider.createToken(TestDataFactory.getSavedUser().getId(), TokenType.ACCESS);

        assertThrows(InvalidTokenTypeException.class, () -> service.refreshToken(access));
    }

    @Test
    void refreshRejectsMalformedToken() {
        JwtTokenProvider provider = new JwtTokenProvider("test-secret");
        AuthService service = new AuthService(userService, passwordEncoder, provider, refreshSessionRepository);

        assertThrows(BadCredentialsException.class, () -> service.refreshToken("invalid"));
    }
}
