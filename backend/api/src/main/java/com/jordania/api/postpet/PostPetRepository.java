package com.jordania.api.postpet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostPetRepository extends JpaRepository<PostPet, UUID> {
    List<PostPet> findByPostId(UUID postId);

    List<PostPet> findByPetId(UUID petId);
}