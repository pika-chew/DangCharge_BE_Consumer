package com.picka_chew.dangcharge_Consumer.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 검증 설정. producer가 개인키로 RS256 서명한 토큰을 공개키만으로 검증한다.
 *
 * @param publicKey X.509 RSA 공개키(PEM 또는 Base64). producer의 JWT_PUBLIC_KEY와 같은 값이다
 * @param issuer 설정하지 않으면 dangcharge. producer의 issuer와 같아야 한다
 */
@ConfigurationProperties("dangcharge.jwt")
public record JwtProperties(String issuer, String publicKey) {

	public JwtProperties {
		issuer = issuer == null || issuer.isBlank() ? "dangcharge" : issuer;
	}

	public boolean hasPublicKey() {
		return publicKey != null && !publicKey.isBlank();
	}

}
