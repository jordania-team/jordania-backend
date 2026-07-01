package com.jordania.api.tutor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TutorRepository extends JpaRepository<Tutor, UUID> {

    @Query("select t from Tutor t where t.user.id = :user_id")
    Optional<Tutor> findByUserId(@Param("user_id") UUID user_id);

    Optional<Tutor> findByUsername(String username);
}
