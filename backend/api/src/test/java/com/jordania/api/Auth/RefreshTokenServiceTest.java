package com.jordania.api.Auth;

import com.jordania.api.User.Providers;
import com.jordania.api.User.RoleType;
import com.jordania.api.User.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(repository, Duration.ofDays(30));
    }

    @Test
    void issueStoresOnlyTokenHashAndReturnsRawToken() {
        Users user = user(UUID.randomUUID());

        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(issued.raw()).isNotBlank();
        assertThat(issued.expiresAt()).isAfter(Instant.now().plus(Duration.ofDays(29)));
        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getTokenHash()).isNotEqualTo(issued.raw());
        assertThat(saved.getUser()).isSameAs(user);
    }

    @Test
    void consumeReturnsActiveToken() {
        Users user = user(UUID.randomUUID());
        String raw = "raw-refresh-token";
        RefreshToken token = new RefreshToken(
                refreshTokenService.sha256(raw),
                UUID.randomUUID(),
                user,
                Instant.now().plusSeconds(3600)
        );

        when(repository.findByTokenHash(refreshTokenService.sha256(raw))).thenReturn(Optional.of(token));

        assertThat(refreshTokenService.consume(raw)).isSameAs(token);
    }

    @Test
    void consumeRejectsExpiredTokenWithoutRevokingFamily() {
        Users user = user(UUID.randomUUID());
        String raw = "raw-refresh-token";
        UUID familyId = UUID.randomUUID();
        RefreshToken token = new RefreshToken(
                refreshTokenService.sha256(raw),
                familyId,
                user,
                Instant.now().minusSeconds(1)
        );

        when(repository.findByTokenHash(refreshTokenService.sha256(raw))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.consume(raw))
                .isInstanceOf(InvalidIdentityTokenException.class)
                .hasMessage("Refresh token is expired");
        verify(repository, never()).revokeFamily(familyId);
    }

    @Test
    void consumeRevokesFamilyWhenTokenWasAlreadyRevoked() {
        Users user = user(UUID.randomUUID());
        String raw = "raw-refresh-token";
        UUID familyId = UUID.randomUUID();
        RefreshToken token = new RefreshToken(
                refreshTokenService.sha256(raw),
                familyId,
                user,
                Instant.now().plusSeconds(3600)
        );
        token.revoke();

        when(repository.findByTokenHash(refreshTokenService.sha256(raw))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.consume(raw))
                .isInstanceOf(InvalidIdentityTokenException.class)
                .hasMessage("Refresh token was reused; session revoked");
        verify(repository).revokeFamily(familyId);
    }

    @Test
    void consumeRejectsUnknownToken() {
        String raw = "raw-refresh-token";
        when(repository.findByTokenHash(refreshTokenService.sha256(raw))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.consume(raw))
                .isInstanceOf(InvalidIdentityTokenException.class)
                .hasMessage("Refresh token is invalid");
    }

    private Users user(UUID userId) {
        return new Users(
                userId,
                RoleType.tutor,
                new Providers("provider-" + userId, "google"),
                "user@example.com",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
    }
}
