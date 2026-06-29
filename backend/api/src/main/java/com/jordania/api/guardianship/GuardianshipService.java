package com.jordania.api.guardianship;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jordania.api.pet.Pet;
import com.jordania.api.pet.PetRepository;
import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuardianshipService {
    private final GuardianshipRepository guardianshipRepository;
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;


    public Guardianship createGuardianship(Tutor tutor, Pet pet) {
        Guardianship guardianship = new Guardianship();
        guardianship.setCreated_at(LocalDateTime.now());
        guardianship.setId(UUID.randomUUID());

        guardianship.setTutor(tutor);
        guardianship.setPet(pet);

        guardianshipRepository.save(guardianship);

        return guardianship;
    }

    public Optional<Guardianship> findByTutor_id(UUID tutor_id) {
        return guardianshipRepository.findByTutorId(tutor_id);
    }

    public Optional<Guardianship> findByPet_id(UUID pet_id) {
        return guardianshipRepository.findByPetId(pet_id);
    }
}
