package com.jordania.api.Pet;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, UUID>{
    
}
