package com.rodrigoborges.api;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.jordania.api.Tutor.Tutor;
import com.jordania.api.Tutor.TutorDTO.TutorRequestDTO;
import com.jordania.api.Tutor.TutorRepository;
import com.jordania.api.Tutor.TutorService;

public class TutorServiceUnitTest {

    @Mock
    private TutorRepository tutorRepository;

    @InjectMocks
    private TutorService tutorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deveSalvarEBuscarPorIdComSucesso() {
        TutorRequestDTO requestDto = new TutorRequestDTO(
            "Jordânia Dev", "jordania_dto_test", false, null, null
        );

        doNothing().when(tutorRepository).insertQuery(any(Tutor.class));

        when(tutorRepository.searchByIdQuery(any(UUID.class)))
            .thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                Tutor t = new Tutor();
                t.setId(id);
                t.setName("Jordânia Dev");
                t.setUsername("jordania_dto_test");
                t.setIsPrivate(false);
                return Optional.of(t);
            });

        Tutor savedTutor = tutorService.createTutor(requestDto);
        UUID idGenerated = savedTutor.getId();

        Tutor fetchedTutor = tutorService.findTutorById(idGenerated);

        Assertions.assertNotNull(idGenerated);
        Assertions.assertEquals("Jordânia Dev", fetchedTutor.getName());

        verify(tutorRepository, times(1)).insertQuery(any(Tutor.class));
        verify(tutorRepository, times(1)).searchByIdQuery(idGenerated);
    }
}