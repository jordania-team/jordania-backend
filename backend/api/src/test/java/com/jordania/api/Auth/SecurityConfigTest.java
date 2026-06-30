package com.jordania.api.Auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void jwtAuthenticationConverterMapsRoleClaimToSpringRoleAuthority() {
        SecurityConfig securityConfig = new SecurityConfig();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuer("pocapi")
                .subject("user-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("role", "tutor")
                .build();

        AbstractAuthenticationToken authentication =
                securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .contains(new SimpleGrantedAuthority("ROLE_TUTOR"));
    }

    @Test
    void jwtAuthenticationConverterKeepsAlreadyPrefixedRoleAuthority() {
        SecurityConfig securityConfig = new SecurityConfig();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuer("pocapi")
                .subject("user-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("role", "ROLE_ADMIN")
                .build();

        AbstractAuthenticationToken authentication =
                securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
