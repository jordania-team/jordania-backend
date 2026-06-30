package com.jordania.api.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TutorsRepository extends JpaRepository<Tutors, UUID> {

    @Query("select t from Tutors t where t.user.id = :user_id")
    Optional<Tutors> findByUserId(@Param("user_id") UUID user_id);

    Optional<Tutors> findByUsername(String username);
}
