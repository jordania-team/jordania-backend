package com.jordania.api.Guardianship;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianshipRepository extends JpaRepository<Guardianship, UUID> {
    public Optional<Guardianship> findByTutorId(UUID tutor_id);
    public Optional<Guardianship> findByPetId(UUID pet_id);
}
