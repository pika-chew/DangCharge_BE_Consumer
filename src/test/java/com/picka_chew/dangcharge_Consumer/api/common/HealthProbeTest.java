package com.picka_chew.dangcharge_Consumer.api.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import com.picka_chew.dangcharge_Consumer.security.TestJwtIssuer;

/**
 * k8s probe가 파드에 직접 부르는 헬스 체크 경로. readiness는 실제 MongoDB 연결까지 본다.
 * Kafka 리스너는 이 테스트와 관계없어 끈다. Docker가 없으면 건너뛰지 않고 실패한다.
 */
@Testcontainers
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@AutoConfigureMockMvc
class HealthProbeTest {

	private static final TestJwtIssuer TOKENS = new TestJwtIssuer();

	@Container
	@ServiceConnection
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

	@Autowired
	MockMvc mockMvc;

	@DynamicPropertySource
	static void jwtPublicKey(DynamicPropertyRegistry registry) {
		registry.add("dangcharge.jwt.public-key", TOKENS::publicKeyBase64);
	}

	@ParameterizedTest
	@ValueSource(strings = {"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"})
	void probesAreUpWithoutLogin(String path) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"/actuator/health", "/actuator/health/readiness"})
	void healthDoesNotExposeDetails(String path) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components").doesNotExist())
				.andExpect(jsonPath("$.details").doesNotExist());
	}

	/** 헬스 체크 말고는 노출하지 않는다. 로그인하지 않으면 공통 포맷 401, 로그인해도 경로가 없다 */
	@ParameterizedTest
	@ValueSource(strings = {"/actuator/env", "/actuator/beans"})
	void otherActuatorEndpointsAreClosed(String path) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
		mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, TOKENS.bearer(7L)))
				.andExpect(status().isNotFound());
	}

}
