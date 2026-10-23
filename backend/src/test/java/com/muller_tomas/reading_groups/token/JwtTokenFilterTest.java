package com.muller_tomas.reading_groups.token;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class JwtTokenFilterTest {
	@Test
	public void previewPathsSkipJwtWhileJoiningAndMemberResourcesStillRequireIt() {
		JwtTokenFilter filter = new JwtTokenFilter(null,
				List.of("/api/groups/join/*/preview", "/api/groups/join/*/icon", "/api/users",
						"/api/registration-rules", "/swagger-ui/**"), null);
		assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/groups/join/token/preview")));
		assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/groups/join/token/icon")));
		assertFalse(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/groups/join/token")));
		assertFalse(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/groups/7/icon")));
		assertFalse(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/users/me")));
		assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/swagger-ui/index.html")));
		assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/registration-rules")));
	}
}
