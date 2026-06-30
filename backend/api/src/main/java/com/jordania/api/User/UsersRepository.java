package com.jordania.api.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    @Query("select u from Users u where u.provider.provider_subject = :provider_subject")
    Optional<Users> findByProviderProviderSubject(@Param("provider_subject") String provider_subject);
}
