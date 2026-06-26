package com.rodrigoborges.api.Pet;

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

import com.jordania.api.Pet.Pet;
import com.jordania.api.Pet.PetDTO.PetRequestDTO;
import com.jordania.api.Pet.PetRepository;
import com.jordania.api.Pet.PetService;

public class PetServiceUnitTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void mustSaveAndSearchPet() {
        PetRequestDTO requestDto = new PetRequestDTO(
            "dog_user", "dog_name", null, null, null
        );

        // doNothing().when(petRepository).save(any(Pet.class));
        petRepository.save(any(Pet.class));

        when(petRepository.findById(any(UUID.class)))
            .thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                Pet pet = new Pet();
                pet.setId(id);
                pet.setName("dog_name");
                pet.setUsername("dog_user");
                return Optional.of(pet);
            });

        Pet savedPet = petService.createPet(requestDto);
        UUID idGenerated = savedPet.getId();

        Pet fetchedTutor = petService.findPetById(idGenerated);

        Assertions.assertNotNull(idGenerated);
        Assertions.assertEquals("dog_name", fetchedTutor.getName());

        verify(petRepository, times(1)).save(any(Pet.class));
        verify(petRepository, times(1)).findById(idGenerated);
    }
}