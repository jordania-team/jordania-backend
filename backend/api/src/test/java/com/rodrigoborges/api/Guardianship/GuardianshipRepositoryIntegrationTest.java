package com.rodrigoborges.api.Guardianship;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import com.jordania.api.Guardianship.Guardianship;
import com.jordania.api.Guardianship.GuardianshipRepository;


@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableJpaRepositories(basePackages = "com.jordania.api.Guardianship") 
@EntityScan(basePackages = "com.jordania.api.Guardianship")
public class GuardianshipRepositoryIntegrationTest {

    @Autowired
    private GuardianshipRepository guardianshipRepository;

    @Test
    public void mustPersistGuardianshipInDatabase() {
        Guardianship guardianship = new Guardianship();
        guardianship.setId(UUID.randomUUID());
        guardianship.setCreated_at(LocalDateTime.now());

        Guardianship saved = guardianshipRepository.save(guardianship);

        Optional<Guardianship> fetched = guardianshipRepository.findById(saved.getId());

        Assertions.assertTrue(fetched.isPresent());
    }
}