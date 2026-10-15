package com.picka_chew.dangcharge_Consumer.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

/** producer JwtTokenIssuer와 같은 형식으로 토큰을 만든다. consumer에는 publicKeyBase64()만 설정한다 */
public final class TestJwtIssuer {

	private final KeyPair keyPair;
	private final JwtEncoder encoder;

	public TestJwtIssuer() {
		this.keyPair = generateKeyPair();
		this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(
				new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
						.privateKey((RSAPrivateKey) keyPair.getPrivate())
						.build())));
	}

	public String publicKeyBase64() {
		return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
	}

	public String bearer(Long userId) {
		return "Bearer " + issue(userId, "access", "dangcharge");
	}

	public String issue(Long userId, String tokenType, String issuer) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(String.valueOf(userId))
				.id(UUID.randomUUID().toString())
				.issuedAt(now)
				.expiresAt(now.plus(Duration.ofMinutes(30)))
				.claim("token_type", tokenType)
				.build();
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
				.getTokenValue();
	}

	private static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

}
