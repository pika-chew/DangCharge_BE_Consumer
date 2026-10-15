package com.picka_chew.dangcharge_Consumer.security.config;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	/** producer의 JwtConfig와 같은 클레임 이름과 값이다 */
	public static final String TOKEN_TYPE_CLAIM = "token_type";
	public static final String ACCESS = "access";

	/**
	 * 요청 인증용. producer가 발급한 액세스 토큰이어야 한다.
	 * 로그아웃 목록은 producer DB에만 있어 여기서는 확인하지 못한다. 로그아웃한 토큰도 만료(30분) 전까지는 조회에 쓸 수 있다.
	 */
	@Bean
	public JwtDecoder accessTokenDecoder(JwtProperties properties) throws Exception {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey(properties)).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()),
				jwt -> ACCESS.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))
						? OAuth2TokenValidatorResult.success()
						: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "토큰 종류가 다릅니다", null))));
		return decoder;
	}

	private static RSAPublicKey publicKey(JwtProperties properties) throws Exception {
		if (!properties.hasPublicKey()) {
			log.warn("JWT 공개키가 설정되지 않아 임시 키로 검증합니다. 로그인이 필요한 API는 모두 401이 되니 "
					+ "producer와 같은 JWT_PUBLIC_KEY를 설정하세요.");
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return (RSAPublicKey) generator.generateKeyPair().getPublic();
		}
		String body = properties.publicKey().replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
		return (RSAPublicKey) KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(body)));
	}

}
