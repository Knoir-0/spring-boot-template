package com.knoir.spring_boot_template.filter;

import java.io.IOException;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(10)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	public static final String REQUEST_TOKEN_ATTRIBUTE = "token";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final List<String> PUBLIC_API_PREFIXES = List.of(
		"/api/public-route-example"
	);

	private final String jwtSecret;

	public JwtAuthenticationFilter(@Value("${jwt.secret}") String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (!path.equals("/api") && !path.startsWith("/api/")) {
			return true;
		}
		for (String prefix : PUBLIC_API_PREFIXES) {
			if (path.equals(prefix) || path.startsWith(prefix + "/")) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = extractToken(request);

		// Missing Token - 401 Unauthorized
		if (token == null || token.isBlank()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		try {
			// Verify & Decode Token
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
			DecodedJWT decoded = verifier.verify(token);
			request.setAttribute(REQUEST_TOKEN_ATTRIBUTE, decoded);
			filterChain.doFilter(request, response);
		} catch (TokenExpiredException error) {
			// 401 - Token Expired (use refresh token)
			writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
		} catch (InvalidClaimException error) {
			// 403 - Token Not Yet Active (nbf) or Invalid Claim
			if (error.getMessage() != null && error.getMessage().contains("can't be used before")) {
				writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Token not yet active");
			} else {
				writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid token");
			}
		} catch (JWTVerificationException error) {
			// 403 - Token Parse / Verification Error
			writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid token");
		} catch (Exception error) {
			// Unknown Error
			log.error("[Unknown JWT error]:", error);
			writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}

	private String extractToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			return authorization.substring(BEARER_PREFIX.length()).trim();
		}

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"error\":\"" + message + "\"}");
	}
}
