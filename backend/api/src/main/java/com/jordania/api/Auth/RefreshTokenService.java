package com.jordania.api.Auth;

import com.jordania.api.User.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Duration refreshTtl;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${app.auth.refresh-ttl:P30D}") Duration refreshTtl
    ) {
        this.repository = repository;
        this.refreshTtl = refreshTtl;
    }

    @Transactional
    public IssuedRefreshToken issue(Users user) {
        return issue(user, UUID.randomUUID());
    }

    @Transactional
    public IssuedRefreshToken issue(Users user, UUID familyId) {
        String raw = generateRaw();
        String hash = sha256(raw);
        Instant expiresAt = Instant.now().plus(refreshTtl);

        repository.save(new RefreshToken(hash, familyId, user, expiresAt));

        return new IssuedRefreshToken(raw, expiresAt);
    }

    @Transactional
    public RefreshToken consume(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken token = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidIdentityTokenException("Refresh token is invalid"));

        if (token.isRevoked()) {
            repository.revokeFamily(token.getFamilyId());
            throw new InvalidIdentityTokenException("Refresh token was reused; session revoked");
        }

        if (!token.isActive()) {
            throw new InvalidIdentityTokenException("Refresh token is expired");
        }

        return token;
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String generateRaw() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record IssuedRefreshToken(String raw, Instant expiresAt) {
    }
}
