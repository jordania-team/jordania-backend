package com.jordania.api.interaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {

    Optional<Interaction> findByPostIdAndTutorId(UUID postId, UUID tutorId);

    List<Interaction> findByPostId(UUID postId);

    List<Interaction> findByTutorId(UUID tutorId);
}