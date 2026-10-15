package com.picka_chew.dangcharge_Consumer.api.common;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.picka_chew.dangcharge_Consumer.api.user.dto.UserResponse;
import com.picka_chew.dangcharge_Consumer.common.config.SwaggerConfig;
import com.picka_chew.dangcharge_Consumer.domain.vehicle.entity.VehicleModel;

/**
 * k8s에서는 Traefik이 /query를 떼고(StripPrefix) X-Forwarded-Prefix: /query를 붙여 넘긴다.
 * 운영의 SERVER_FORWARD_HEADERS_STRATEGY=framework처럼 ForwardedHeaderFilter를 가장 먼저 두고,
 * Swagger 주소에는 /query가 다시 붙고 인증 규칙은 그대로인지 확인한다.
 */
@Import({SwaggerConfig.class, ForwardedPrefixTest.ForwardedHeaders.class})
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class, org.springdoc.webmvc.ui.SwaggerConfig.class,
		SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class})
class ForwardedPrefixTest extends ControllerTestSupport {

	private static final String PREFIX = "/query";

	@Test
	void swaggerRedirectKeepsPrefix() throws Exception {
		mockMvc.perform(viaIngress("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/query/swagger-ui/index.html"));
	}

	@Test
	void swaggerLoadsApiDocsThroughPrefix() throws Exception {
		mockMvc.perform(viaIngress("/v3/api-docs/swagger-config"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("/query/v3/api-docs"))
				.andExpect(jsonPath("$.configUrl").value("/query/v3/api-docs/swagger-config"));
		mockMvc.perform(viaIngress("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.servers[0].url").value("http://localhost/query"));
	}

	/** Security 규칙은 context path(/query)를 뺀 경로로 비교하므로 헤더가 있어도 공개·로그인 필요 구분이 같다 */
	@Test
	void accessRulesStayTheSameWithPrefixHeader() throws Exception {
		given(getMyInfoUseCase.execute(7L)).willReturn(new UserResponse(7L, "driver", "운전자", VehicleModel.TESLA));

		mockMvc.perform(viaIngress("/api/v1/stations/1"))
				.andExpect(status().isOk());
		mockMvc.perform(viaIngress("/api/v1/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
		mockMvc.perform(viaIngress("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.loginId").value("driver"));
	}

	/** Traefik이 앞부분을 떼고 넘긴 요청. 앱이 받는 경로에는 /query가 없다 */
	private static MockHttpServletRequestBuilder viaIngress(String path) {
		return get(path).header("X-Forwarded-Prefix", PREFIX);
	}

	@TestConfiguration
	static class ForwardedHeaders {

		@Bean
		FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
			FilterRegistrationBean<ForwardedHeaderFilter> registration =
					new FilterRegistrationBean<>(new ForwardedHeaderFilter());
			registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return registration;
		}

	}

}
