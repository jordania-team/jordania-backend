package com.jordania.api.pet;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, UUID>{

    List<Pet> findByUsername(String username);
    boolean existsByUsername(String username);
    // Optional<Pet> findById(UUID id);
    
}
