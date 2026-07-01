package com.jordania.api.User;

import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TutorRepository tutorsRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getMeReturnsBasicSessionWithTutorNameWhenItExists() {
        UUID userId = UUID.randomUUID();
        Users user = user(userId);
        Tutor tutor = new Tutor(
                user,
                "Tutor Name",
                "tutor.name",
                false,
                null,
                LocalDateTime.now().minusYears(20)
        );

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tutorsRepository.findByUserId(userId)).thenReturn(Optional.of(tutor));

        UserResponse response = userService.getMe(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.provider()).isEqualTo("google");
        assertThat(response.role()).isEqualTo("tutor");
        assertThat(response.name()).isEqualTo("Tutor Name");
    }

    @Test
    void getMeReturnsNullNameBeforeTutorExists() {
        UUID userId = UUID.randomUUID();
        Users user = user(userId);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tutorsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThat(userService.getMe(userId).name()).isNull();
    }

    @Test
    void getMeReturnsNotFoundWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(usersRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(userId))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
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
