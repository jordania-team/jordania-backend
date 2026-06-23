package com.jordania.api.Auth;

import com.jordania.api.User.Usuario;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by", length = 64)
    private String replacedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected RefreshToken() {}

    public RefreshToken(String tokenHash, UUID familyId, Usuario user, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.familyId  = familyId;
        this.user      = user;
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    // Getters
    public Long    getId()         { return id; }
    public String  getTokenHash()  { return tokenHash; }
    public UUID    getFamilyId()   { return familyId; }
    public Usuario getUser()       { return user; }
    public Instant getExpiresAt()  { return expiresAt; }
    public Instant getRevokedAt()  { return revokedAt; }
    public String  getReplacedBy() { return replacedBy; }

    // Mutation
    public void revoke() {
        this.revokedAt = Instant.now();
    }

    public void replacedBy(String newHash) {
        this.replacedBy = newHash;
        this.revokedAt  = Instant.now();
    }
}
