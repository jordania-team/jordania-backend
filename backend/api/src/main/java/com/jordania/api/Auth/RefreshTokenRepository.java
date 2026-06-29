package com.jordania.api.Auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = CURRENT_TIMESTAMP where r.familyId = :familyId and r.revokedAt is null")
    void revokeFamily(UUID familyId);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = CURRENT_TIMESTAMP where r.user.id = :userId and r.revokedAt is null")
    void revokeAllForUser(UUID userId);
}
