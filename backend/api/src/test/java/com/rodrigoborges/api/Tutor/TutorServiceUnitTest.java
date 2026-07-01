package com.rodrigoborges.api.Tutor;

import com.jordania.api.Storage.ImageStorageService;
import com.jordania.api.User.Providers;
import com.jordania.api.User.RoleType;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;
import com.jordania.api.tutor.TutorService;
import com.jordania.api.tutor.dto.TutorRequest;
import com.jordania.api.tutor.dto.TutorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorServiceUnitTest {

    private static final LocalDateTime BIRTHDAY = LocalDateTime.of(1990, 1, 1, 0, 0);

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private TutorService tutorService;

    @Test
    void mustSaveAndSearchTutor() {
        UUID userId = UUID.randomUUID();
        Users user = user(userId);
        TutorRequest request = new TutorRequest(
                "Jordania Dev",
                "jordania.test",
                false,
                null,
                BIRTHDAY
        );

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tutorRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(tutorRepository.findByUsername("jordania.test")).thenReturn(Optional.empty());
        when(tutorRepository.save(any(Tutor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TutorResponse savedTutor = tutorService.saveMe(userId, request);

        ArgumentCaptor<Tutor> tutorCaptor = ArgumentCaptor.forClass(Tutor.class);
        verify(tutorRepository, times(1)).save(tutorCaptor.capture());
        Tutor persistedTutor = tutorCaptor.getValue();

        when(tutorRepository.findByUserId(userId)).thenReturn(Optional.of(persistedTutor));

        TutorResponse fetchedTutor = tutorService.findMe(userId);

        Assertions.assertNotNull(savedTutor.id());
        Assertions.assertEquals("Jordania Dev", fetchedTutor.name());
        Assertions.assertEquals("jordania.test", fetchedTutor.username());
        Assertions.assertEquals(userId, fetchedTutor.user_id());
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
