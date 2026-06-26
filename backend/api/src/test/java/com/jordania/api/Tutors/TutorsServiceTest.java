package com.jordania.api.Tutors;

import com.jordania.api.User.Providers;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorsServiceTest {

    private static final LocalDateTime BIRTHDAY = LocalDateTime.of(1990, 1, 1, 0, 0);

    @Mock
    private TutorsRepository tutorsRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private TutorsService tutorsService;

    @Test
    void findMeReturnsNotFoundWhenTutorDoesNotExist() {
        UUID user_id = UUID.randomUUID();
        when(tutorsRepository.findByUserId(user_id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tutorsService.findMe(user_id))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void saveMeCreatesTutorWithNormalizedUsername() {
        UUID user_id = UUID.randomUUID();
        Users user = user(user_id);
        TutorsRequest request = new TutorsRequest(
                "Rodrigo Borges",
                " Rodrigo.Borges ",
                false,
                " https://example.com/avatar.png ",
                BIRTHDAY
        );

        when(usersRepository.findById(user_id)).thenReturn(Optional.of(user));
        when(tutorsRepository.findByUserId(user_id)).thenReturn(Optional.empty());
        when(tutorsRepository.findByUsername("rodrigo.borges")).thenReturn(Optional.empty());
        when(tutorsRepository.save(any(Tutors.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TutorsResponse response = tutorsService.saveMe(user_id, request);

        ArgumentCaptor<Tutors> tutorsCaptor = ArgumentCaptor.forClass(Tutors.class);
        org.mockito.Mockito.verify(tutorsRepository).save(tutorsCaptor.capture());
        assertThat(tutorsCaptor.getValue().getUser().getId()).isEqualTo(user_id);
        assertThat(tutorsCaptor.getValue().getUsername()).isEqualTo("rodrigo.borges");
        assertThat(tutorsCaptor.getValue().getName()).isEqualTo("Rodrigo Borges");
        assertThat(tutorsCaptor.getValue().getImg_url()).isEqualTo("https://example.com/avatar.png");

        assertThat(response.user_id()).isEqualTo(user_id);
        assertThat(response.username()).isEqualTo("rodrigo.borges");
        assertThat(response.is_private()).isFalse();
        assertThat(response.reports_counter()).isZero();
    }

    @Test
    void saveMeUpdatesExistingTutor() {
        UUID user_id = UUID.randomUUID();
        Users user = user(user_id);
        Tutors existing = new Tutors(
                user,
                "Old Name",
                "old.name",
                false,
                null,
                BIRTHDAY
        );
        TutorsRequest request = new TutorsRequest(
                "New Name",
                "new.name",
                true,
                null,
                BIRTHDAY.plusYears(1)
        );

        when(usersRepository.findById(user_id)).thenReturn(Optional.of(user));
        when(tutorsRepository.findByUserId(user_id)).thenReturn(Optional.of(existing));
        when(tutorsRepository.findByUsername("new.name")).thenReturn(Optional.empty());
        when(tutorsRepository.save(existing)).thenReturn(existing);

        TutorsResponse response = tutorsService.saveMe(user_id, request);

        assertThat(response.id()).isEqualTo(existing.getId());
        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.username()).isEqualTo("new.name");
        assertThat(response.is_private()).isTrue();
    }

    @Test
    void saveMeReturnsConflictWhenUsernameBelongsToAnotherTutor() {
        UUID user_id = UUID.randomUUID();
        Users user = user(user_id);
        Tutors other = new Tutors(
                user(UUID.randomUUID()),
                "Other Tutor",
                "taken.name",
                false,
                null,
                BIRTHDAY
        );
        TutorsRequest request = new TutorsRequest(
                "Rodrigo Borges",
                "taken.name",
                false,
                null,
                BIRTHDAY
        );

        when(usersRepository.findById(user_id)).thenReturn(Optional.of(user));
        when(tutorsRepository.findByUserId(user_id)).thenReturn(Optional.empty());
        when(tutorsRepository.findByUsername("taken.name")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> tutorsService.saveMe(user_id, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    private Users user(UUID user_id) {
        return new Users(
                user_id,
                RoleType.tutor,
                new Providers("provider-" + user_id, "google"),
                "user@example.com",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
    }
}
