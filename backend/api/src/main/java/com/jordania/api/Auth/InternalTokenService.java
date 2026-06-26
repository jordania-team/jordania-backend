package com.jordania.api.Auth;

import com.jordania.api.User.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class InternalTokenService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration tokenTtl;

    public InternalTokenService(
            JwtEncoder encoder,
            @Value("${app.auth.issuer}") String issuer,
            @Value("${app.auth.token-ttl}") Duration tokenTtl
    ) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.tokenTtl = tokenTtl;
    }

    public String issue(Users user, String name) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(tokenTtl))
                .subject(user.getId().toString())
                .claim("provider", user.getProvider().getProvider_name())
                .claim("role", user.getRole().name());

        if (name != null) {
            claims.claim("name", name);
        }
        if (user.getEmail() != null) {
            claims.claim("email", user.getEmail());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
