package com.knoir.spring_boot_template;

import java.time.Instant;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.knoir.spring_boot_template.exception.ApiError;
import com.knoir.spring_boot_template.exception.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringBootTemplateApplicationTests {

	private static final String SECRET = "local";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Test
	void contextLoads() {
		assertThat(mockMvc).isNotNull();
	}

	@Test
	void healthCheckReturnsSystemMetrics() throws Exception {
		mockMvc.perform(get("/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"))
			.andExpect(jsonPath("$.timestamp").exists())
			.andExpect(jsonPath("$.app.version").exists())
			.andExpect(jsonPath("$.app.environment").value("local"))
			.andExpect(jsonPath("$.runtime.javaVersion").exists())
			.andExpect(jsonPath("$.appUptimeSeconds").isNumber())
			.andExpect(jsonPath("$.os.platform").exists())
			.andExpect(jsonPath("$.os.totalMemoryBytes").isNumber())
			.andExpect(jsonPath("$.processMemory.heapUsedBytes").isNumber());
	}

	@Test
	void publicRoutesAreAccessibleWithoutJwt() throws Exception {
		mockMvc.perform(get("/api/public-route-example"))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/public-route-example/123"))
			.andExpect(status().isOk())
			.andExpect(content().string("123"));
	}

	@Test
	void privateRoutesRejectMissingTokenWith401() throws Exception {
		mockMvc.perform(get("/api/private-route-example"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void privateRoutesAllowValidBearerToken() throws Exception {
		String token = JWT.create()
			.withSubject("user-1")
			.withExpiresAt(Instant.now().plusSeconds(3600))
			.sign(Algorithm.HMAC256(SECRET));

		mockMvc.perform(get("/api/private-route-example")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/private-route-example/abc")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(content().string("abc"));
	}

	@Test
	void privateRoutesAllowValidAccessTokenCookie() throws Exception {
		String token = JWT.create()
			.withSubject("user-1")
			.withExpiresAt(Instant.now().plusSeconds(3600))
			.sign(Algorithm.HMAC256(SECRET));

		mockMvc.perform(get("/api/private-route-example/cookie-id")
				.cookie(new Cookie("access_token", token)))
			.andExpect(status().isOk())
			.andExpect(content().string("cookie-id"));
	}

	@Test
	void privateRoutesRejectExpiredTokenWith401() throws Exception {
		String expiredToken = JWT.create()
			.withSubject("user-1")
			.withExpiresAt(Instant.now().minusSeconds(60))
			.sign(Algorithm.HMAC256(SECRET));

		mockMvc.perform(get("/api/private-route-example")
				.header("Authorization", "Bearer " + expiredToken))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error").value("Token expired"));
	}

	@Test
	void privateRoutesRejectNotYetActiveTokenWith403() throws Exception {
		String prematureToken = JWT.create()
			.withSubject("user-1")
			.withNotBefore(Instant.now().plusSeconds(3600))
			.sign(Algorithm.HMAC256(SECRET));

		mockMvc.perform(get("/api/private-route-example")
				.header("Authorization", "Bearer " + prematureToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("Token not yet active"));
	}

	@Test
	void privateRoutesRejectInvalidTokenWith403() throws Exception {
		mockMvc.perform(get("/api/private-route-example")
				.header("Authorization", "Bearer invalid.jwt.token"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("Invalid token"));
	}

	@Test
	void unknownRouteReturns404() throws Exception {
		mockMvc.perform(get("/non-existent-route"))
			.andExpect(status().isNotFound());
	}

	@Test
	void payloadOver2MbReturns413() throws Exception {
		byte[] largePayload = new byte[(2 * 1024 * 1024) + 10];
		mockMvc.perform(post("/api/public-route-example")
				.contentType(MediaType.APPLICATION_JSON)
				.content(largePayload))
			.andExpect(status().is(413));
	}

	@Test
	void globalExceptionHandlerFormatsApiErrorCorrectlyByEnvironment() {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid input", Map.of("field", "email"));

		ReflectionTestUtils.setField(globalExceptionHandler, "environmentName", "local");
		ResponseEntity<Map<String, Object>> localResponse = globalExceptionHandler.handleApiError(apiError);
		assertThat(localResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(localResponse.getBody()).containsEntry("error", "Invalid input");
		assertThat(localResponse.getBody()).containsKey("stack");

		ReflectionTestUtils.setField(globalExceptionHandler, "environmentName", "prd");
		ResponseEntity<Map<String, Object>> prdResponse = globalExceptionHandler.handleApiError(apiError);
		assertThat(prdResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(prdResponse.getBody()).containsEntry("error", "Invalid input");
		assertThat(prdResponse.getBody()).doesNotContainKey("stack");
		ReflectionTestUtils.setField(globalExceptionHandler, "environmentName", "local");
	}
}
