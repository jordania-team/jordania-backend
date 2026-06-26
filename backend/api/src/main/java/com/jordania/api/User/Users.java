package com.jordania.api.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class Users {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "role_type")
    private RoleType role;

    @Column(length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, referencedColumnName = "provider_subject")
    private Providers provider;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    protected Users() {
    }

    public Users(Providers provider, String email) {
        this(UUID.randomUUID(), RoleType.tutor, provider, email, LocalDateTime.now(), LocalDateTime.now());
    }

    public Users(
            UUID id,
            RoleType role,
            Providers provider,
            String email,
            LocalDateTime created_at,
            LocalDateTime updated_at
    ) {
        this.id = id;
        this.role = role;
        this.provider = provider;
        this.email = normalized(email);
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public UUID getId() {
        return id;
    }

    public RoleType getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public Providers getProvider() {
        return provider;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void updateEmail(String email) {
        String normalized = normalized(email);
        if (normalized != null) {
            this.email = normalized;
        }
        this.updated_at = LocalDateTime.now();
    }

    private String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
