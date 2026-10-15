package com.picka_chew.dangcharge_Consumer.api.common;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
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
import org.springframework.context.annotation.Import;

import com.picka_chew.dangcharge_Consumer.common.config.SwaggerConfig;

/** 조회 API가 OpenAPI 문서에 producer와 같은 경로로 실리는지 확인한다 */
@Import(SwaggerConfig.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class, org.springdoc.webmvc.ui.SwaggerConfig.class,
		SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class})
class ApiDocsTest extends ControllerTestSupport {

	@Test
	void apiDocsListQueryControllers() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("DangCharge Consumer API"))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/stations/{stationId}/availability")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/charging/history/{reservationId}")))
				.andExpect(jsonPath("$.paths", not(hasKey("/api/v1/charging/{reservationId}/stop"))))
				.andExpect(jsonPath("$.paths['/api/v1/vehicle-models'].get.summary").value("차종 목록"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}

	@Test
	void swaggerUiPathRedirectsToUi() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
	}

}
