package com.rodrigoborges.api.Guardianship;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.jordania.api.Guardianship.Guardianship;
import com.jordania.api.Guardianship.GuardianshipRepository;
import com.jordania.api.Guardianship.GuardianshipService;
import com.jordania.api.Pet.Pet;
import com.jordania.api.Pet.PetRepository;
import com.jordania.api.Tutor.Tutor;
import com.jordania.api.Tutor.TutorRepository;

public class GuardianshipServiceUnitTest {

    @Mock
    private GuardianshipRepository guardianshipRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private GuardianshipService guardianshipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void mustCreateGuardianshipSuccessfully() {
        // Arrange (Configuração do cenário)
        Tutor tutor = new Tutor();
        Pet pet = new Pet();
        
        // Mockando o comportamento do save para retornar a própria entidade enviada
        when(guardianshipRepository.save(any(Guardianship.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Execução do método sob teste)
        Guardianship createdGuardianship = guardianshipService.createGuardianship(tutor, pet);

        // Assert (Verificações dos resultados)
        Assertions.assertNotNull(createdGuardianship);
        Assertions.assertNotNull(createdGuardianship.getId());
        Assertions.assertNotNull(createdGuardianship.getCreated_at());
        Assertions.assertEquals(tutor, createdGuardianship.getTutor());
        Assertions.assertEquals(pet, createdGuardianship.getPet());

        // Verifica se o repositório foi chamado exatamente 1 vez para salvar
        verify(guardianshipRepository, times(1)).save(any(Guardianship.class));
    }

    @Test
    public void mustFindGuardianshipByTutorId() {
        // Arrange
        UUID tutorId = UUID.randomUUID();
        Guardianship mockGuardianship = new Guardianship();
        mockGuardianship.setId(UUID.randomUUID());
        
        when(guardianshipRepository.findByTutorId(tutorId)).thenReturn(Optional.of(mockGuardianship));

        // Act
        Optional<Guardianship> result = guardianshipService.findByTutor_id(tutorId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(mockGuardianship.getId(), result.get().getId());
        verify(guardianshipRepository, times(1)).findByTutorId(tutorId);
    }

    @Test
    public void mustFindGuardianshipByPetId() {
        // Arrange
        UUID petId = UUID.randomUUID();
        Guardianship mockGuardianship = new Guardianship();
        mockGuardianship.setId(UUID.randomUUID());

        when(guardianshipRepository.findByPetId(petId)).thenReturn(Optional.of(mockGuardianship));

        // Act
        Optional<Guardianship> result = guardianshipService.findByPet_id(petId);

        // Assert
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(mockGuardianship.getId(), result.get().getId());
        verify(guardianshipRepository, times(1)).findByPetId(petId);
    }
}