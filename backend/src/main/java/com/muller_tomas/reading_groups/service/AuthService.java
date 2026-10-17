package com.muller_tomas.reading_groups.service;

import java.time.Instant;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.muller_tomas.reading_groups.dto.LoginRequest;
import com.muller_tomas.reading_groups.dto.TokenPairResponse;
import com.muller_tomas.reading_groups.exception.InvalidTokenTypeException;
import com.muller_tomas.reading_groups.exception.TokenRevokedException;
import com.muller_tomas.reading_groups.model.RefreshSession;
import com.muller_tomas.reading_groups.model.User;
import com.muller_tomas.reading_groups.repository.RefreshSessionRepository;
import com.muller_tomas.reading_groups.token.JwtTokenProvider;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

@Service
public class AuthService {
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshSessionRepository refreshSessionRepository;

    public AuthService(UserService userService, BCryptPasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider, RefreshSessionRepository refreshSessionRepository) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshSessionRepository = refreshSessionRepository;
    }

    @Transactional(readOnly = true)
    public String refreshToken(String token) {
        RefreshSession session = requireActiveSession(token);
        return tokenProvider.createToken(session.getUser().getId(), TokenType.ACCESS);
    }

    @Transactional
    public void logoutUser(String refreshToken) {
        RefreshSession session = requireActiveSession(refreshToken);
        session.setRevokedAt(Instant.now());
        refreshSessionRepository.save(session);
    }

    @Transactional
    public TokenPairResponse loginUser(LoginRequest loginRequest) {
        User user = userService.findUserByUsernameOrEmail(loginRequest.getLogin());
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        String accessToken = tokenProvider.createToken(user.getId(), TokenType.ACCESS);
        String refreshToken = tokenProvider.createToken(user.getId(), TokenType.REFRESH);
        refreshSessionRepository.save(new RefreshSession(user, tokenProvider.hashToken(refreshToken),
                tokenProvider.getExpiration(refreshToken)));
        return new TokenPairResponse(accessToken, refreshToken);
    }

    private RefreshSession requireActiveSession(String token) {
        if (!tokenProvider.isTokenValid(token)) {
            throw new BadCredentialsException("Token is invalid");
        }
        if (tokenProvider.getTokenType(token) != TokenType.REFRESH) {
            throw new InvalidTokenTypeException("Invalid token type");
        }

        RefreshSession session = refreshSessionRepository.findByTokenHash(tokenProvider.hashToken(token))
                .orElseThrow(() -> new BadCredentialsException("Refresh session not found"));
        if (session.getRevokedAt() != null) {
            throw new TokenRevokedException("Refresh token has been revoked");
        }
        if (!session.getExpiresAt().isAfter(Instant.now())) {
            throw new BadCredentialsException("Refresh session has expired");
        }
        if (session.getUser().getId() != tokenProvider.getUserId(token)) {
            throw new BadCredentialsException("Refresh session does not match token user");
        }
        return session;
    }
}
