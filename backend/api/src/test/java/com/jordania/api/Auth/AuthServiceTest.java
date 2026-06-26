package com.jordania.api.Auth;

import com.jordania.api.User.Providers;
import com.jordania.api.User.ProvidersRepository;
import com.jordania.api.User.RoleType;
import com.jordania.api.User.Tutors;
import com.jordania.api.User.TutorsRepository;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SocialTokenVerifier tokenVerifier;

    @Mock
    private ProvidersRepository providersRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TutorsRepository tutorsRepository;

    @Mock
    private InternalTokenService internalTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void firstLoginCreatesProviderAndUserOnly() {
        LoginRequest request = new LoginRequest("google", "identity-token", null, null);
        SocialIdentity identity = new SocialIdentity(
                AuthProvider.GOOGLE,
                "google-subject",
                "Taylor",
                "taylor@example.com"
        );

        when(tokenVerifier.verify(request)).thenReturn(identity);
        when(providersRepository.findById("google-subject")).thenReturn(Optional.empty());
        when(providersRepository.save(any(Providers.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usersRepository.findByProviderProviderSubject("google-subject")).thenReturn(Optional.empty());
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tutorsRepository.findByUserId(any(UUID.class))).thenReturn(Optional.empty());
        when(internalTokenService.issue(any(Users.class), eq("Taylor"))).thenReturn("internal-jwt");

        LoginResponse response = authService.login(request);

        ArgumentCaptor<Providers> providerCaptor = ArgumentCaptor.forClass(Providers.class);
        verify(providersRepository).save(providerCaptor.capture());
        assertThat(providerCaptor.getValue().getProvider_subject()).isEqualTo("google-subject");
        assertThat(providerCaptor.getValue().getProvider_name()).isEqualTo("google");

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(RoleType.tutor);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("taylor@example.com");

        assertThat(response.token()).isEqualTo("internal-jwt");
        assertThat(response.userId()).isEqualTo(userCaptor.getValue().getId());
        assertThat(response.name()).isEqualTo("Taylor");
        assertThat(response.email()).isEqualTo("taylor@example.com");
        assertThat(response.role()).isEqualTo("tutor");
    }

    @Test
    void secondLoginReusesExistingProviderAndUserAndReturnsTutorNameWhenExists() {
        UUID userId = UUID.randomUUID();
        Providers providers = new Providers("apple-subject", "apple");
        Users existingUser = new Users(
                userId,
                RoleType.tutor,
                providers,
                "old@example.com",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        Tutors tutors = new Tutors(
                existingUser,
                "Tutor Name",
                "tutor.name",
                false,
                null,
                LocalDateTime.now().minusYears(20)
        );
        LoginRequest request = new LoginRequest("apple", "identity-token", "New Name", "nonce");
        SocialIdentity identity = new SocialIdentity(
                AuthProvider.APPLE,
                "apple-subject",
                "New Name",
                "new@example.com"
        );

        when(tokenVerifier.verify(request)).thenReturn(identity);
        when(providersRepository.findById("apple-subject")).thenReturn(Optional.of(providers));
        when(usersRepository.findByProviderProviderSubject("apple-subject")).thenReturn(Optional.of(existingUser));
        when(usersRepository.save(existingUser)).thenReturn(existingUser);
        when(tutorsRepository.findByUserId(userId)).thenReturn(Optional.of(tutors));
        when(internalTokenService.issue(existingUser, "Tutor Name")).thenReturn("internal-jwt");

        LoginResponse response = authService.login(request);

        verify(providersRepository, never()).save(any(Providers.class));
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Tutor Name");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.role()).isEqualTo("tutor");
    }

    @Test
    void rejectsProviderSubjectCollisionAcrossProviders() {
        LoginRequest request = new LoginRequest("google", "identity-token", null, null);
        SocialIdentity identity = new SocialIdentity(
                AuthProvider.GOOGLE,
                "shared-subject",
                null,
                "user@example.com"
        );

        when(tokenVerifier.verify(request)).thenReturn(identity);
        when(providersRepository.findById("shared-subject"))
                .thenReturn(Optional.of(new Providers("shared-subject", "apple")));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidIdentityTokenException.class);

        verifyNoInteractions(usersRepository, tutorsRepository, internalTokenService);
    }
}
