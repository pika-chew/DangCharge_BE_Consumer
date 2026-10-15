package com.picka_chew.dangcharge_Consumer.common.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.picka_chew.dangcharge_Consumer.common.annotation.CurrentUser;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	public static final String BEARER_SCHEME = "bearerAuth";

	static {
		// 로그인 사용자 파라미터는 토큰에서 채우므로 문서에 노출하지 않는다
		SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
	}

	/** 기본은 Bearer 토큰이 필요하다. 로그인 없이 쓰는 API는 @SecurityRequirements로 비운다 */
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("DangCharge Consumer API")
						.description("DangCharge 조회 API 문서. producer가 발행한 이벤트로 만든 MongoDB 데이터를 조회한다")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}

}
