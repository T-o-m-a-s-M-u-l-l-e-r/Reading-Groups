package com.muller_tomas.reading_groups.token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.muller_tomas.reading_groups.exception.InvalidTokenTypeException;
import com.muller_tomas.reading_groups.token.JwtTokenProvider.TokenType;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final List<String> publicPaths;
	private final TokenExtractor tokenExtractor;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, @Value("${app.public.paths}") List<String> publicPaths,
			TokenExtractor tokenExtractor) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.publicPaths = publicPaths;
		this.tokenExtractor = tokenExtractor;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();

		return publicPaths.stream().anyMatch(publicPath -> pathMatcher.match(publicPath, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String header = request.getHeader("Authorization");
		String token = tokenExtractor.extractTokenFromHeader(header);

		boolean valid = jwtTokenProvider.isTokenValid(token);
		TokenType tokenType;

		try {
			tokenType = jwtTokenProvider.getTokenType(token);
		} catch (IllegalArgumentException e) {
			throw new InvalidTokenTypeException("Invalid token type");
		}

		if (!valid) {
			throw new BadCredentialsException("Invalid token");
		}

		if (tokenType != TokenType.ACCESS) {
			throw new InvalidTokenTypeException("Invalid token type");
		}

		int userId = jwtTokenProvider.getUserId(token);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

}
