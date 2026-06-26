package com.jordania.api.Pet;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jordania.api.Pet.PetDTO.PetRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository repository;

    public Pet createPet(PetRequestDTO dto) {
        Pet pet = new Pet();
        pet.setUsername(dto.username());
        pet.setName(dto.name());
        pet.setImg_url(dto.img_url());
        pet.setBirthday(dto.birthday());
        // pet.setSpecies(dto.species());

        // protected data not on dto
        pet.setId(UUID.randomUUID());
        pet.setUpdated_at(LocalDateTime.now());
        pet.setCreated_at(LocalDateTime.now());

        repository.save(pet);
        return pet;
    }

    public void deletePet(Pet pet) {
        if(repository.existsById(pet.getId())) {
            repository.delete(pet);
        }
    }

    public Pet findPetById(UUID id) {
        return repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pet not found with ID:" + id));
    }
}
