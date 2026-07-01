package com.jordania.api.Auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class SocialTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    private final String appleClientId;
    private final String googleClientId;

    public SocialTokenVerifier(
            @Value("${app.auth.apple-client-id:}") String appleClientId,
            @Value("${app.auth.google-client-id:}") String googleClientId
    ) {
        this.appleClientId = appleClientId;
        this.googleClientId = googleClientId;
    }

    public SocialIdentity verify(LoginRequest request) {
        AuthProvider provider;
        try {
            provider = AuthProvider.from(request.provider());
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdentityTokenException("Unsupported authentication provider");
        }

        return switch (provider) {
            case APPLE -> verifyApple(request);
            case GOOGLE -> verifyGoogle(request);
        };
    }

    private SocialIdentity verifyApple(LoginRequest request) {
        requireConfigured(appleClientId, "AUTH_APPLE_CLIENT_ID");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI).build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                new JwtClaimValidator<>("iss", APPLE_ISSUER::equals);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator,
                new AudienceValidator(appleClientId)
        ));

        Jwt jwt = decode(decoder, request.identityToken());
        validateAppleNonce(jwt, request.rawNonce());

        return new SocialIdentity(
                AuthProvider.APPLE,
                jwt.getSubject(),
                normalized(request.name()),
                jwt.getClaimAsString("email")
        );
    }

    private SocialIdentity verifyGoogle(LoginRequest request) {
        requireConfigured(googleClientId, "AUTH_GOOGLE_CLIENT_ID");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWK_SET_URI).build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                new JwtClaimValidator<>("iss", issuer ->
                        List.of(GOOGLE_ISSUER, "accounts.google.com").contains(issuer));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator,
                new AudienceValidator(googleClientId)
        ));

        Jwt jwt = decode(decoder, request.identityToken());

        return new SocialIdentity(
                AuthProvider.GOOGLE,
                jwt.getSubject(),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("email")
        );
    }

    private Jwt decode(JwtDecoder decoder, String token) {
        try {
            return decoder.decode(token);
        } catch (JwtException exception) {
            throw new InvalidIdentityTokenException("Invalid identity token");
        }
    }

    private void validateAppleNonce(Jwt jwt, String rawNonce) {
        String tokenNonce = jwt.getClaimAsString("nonce");
        if (rawNonce == null || rawNonce.isBlank() || tokenNonce == null) {
            throw new InvalidIdentityTokenException("Apple nonce is required");
        }

        String hashedNonce = sha256(rawNonce);
        if (!MessageDigest.isEqual(
                hashedNonce.getBytes(StandardCharsets.UTF_8),
                tokenNonce.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InvalidIdentityTokenException("Apple nonce is invalid");
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private void requireConfigured(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new AuthConfigurationException(propertyName + " is not configured");
        }
    }

    private String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
