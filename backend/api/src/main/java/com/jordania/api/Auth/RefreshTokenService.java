package com.jordania.api.Auth;

import com.jordania.api.User.Usuario;
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

    /** Emite novo refresh token iniciando uma nova família. */
    @Transactional
    public IssuedRefreshToken issue(Usuario user) {
        return issue(user, UUID.randomUUID());
    }

    /** Rotação: emite novo token mantendo a mesma família. */
    @Transactional
    public IssuedRefreshToken issue(Usuario user, UUID familyId) {
        String raw  = generateRaw();
        String hash = sha256(raw);
        Instant exp = Instant.now().plus(refreshTtl);

        RefreshToken entity = new RefreshToken(hash, familyId, user, exp);
        repository.save(entity);

        return new IssuedRefreshToken(raw, exp);
    }

    /**
     * Valida e consome o refresh token.
     *
     * Reuse detection: se o token já foi rotacionado (revogado com replacedBy != null)
     * revoga a família inteira como defesa contra roubo de token.
     */
    @Transactional
    public RefreshToken consume(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken token = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidIdentityTokenException("Refresh token inválido"));

        if (token.isRevoked()) {
            // Token já usado: possível roubo — revoga família inteira
            repository.revokeFamily(token.getFamilyId());
            throw new InvalidIdentityTokenException(
                    "Refresh token reutilizado — sessão encerrada por segurança");
        }

        if (!token.isActive()) {
            throw new InvalidIdentityTokenException("Refresh token expirado");
        }

        return token;
    }

    /** Revoga todos os tokens ativos do usuário (logout). */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private String generateRaw() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record IssuedRefreshToken(String raw, Instant expiresAt) {}
}
