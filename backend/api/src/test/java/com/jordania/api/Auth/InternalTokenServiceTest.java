package com.jordania.api.Auth;

import com.jordania.api.User.Providers;
import com.jordania.api.User.RoleType;
import com.jordania.api.User.Users;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InternalTokenServiceTest {

    @Test
    void issuedTokenContainsUserProviderRoleAndEmailClaims() {
        byte[] secret = "test-secret-with-at-least-thirty-two-bytes".getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secret, "HmacSHA256");
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        InternalTokenService tokenService = new InternalTokenService(
                encoder,
                "pocapi-test",
                Duration.ofHours(1)
        );
        UUID userId = UUID.randomUUID();
        Users user = new Users(
                userId,
                RoleType.tutor,
                new Providers("google-subject", "google"),
                "taylor@example.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Jwt jwt = decoder.decode(tokenService.issue(user, "Taylor"));

        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("pocapi-test");
        assertThat(jwt.getClaimAsString("provider")).isEqualTo("google");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("tutor");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Taylor");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("taylor@example.com");
    }
}
