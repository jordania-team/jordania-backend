package com.rodrigoborges.api.Guardianship;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import com.jordania.api.guardianship.Guardianship;
import com.jordania.api.guardianship.GuardianshipRepository;
import com.jordania.api.pet.Pet;
import com.jordania.api.tutor.Tutor;


@DataJpaTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableJpaRepositories(basePackages = "com.jordania.api.guardianship") 
@EntityScan(basePackages = {
        "com.jordania.api.guardianship",
        "com.jordania.api.pet",
        "com.jordania.api.tutor",
        "com.jordania.api.User"
})
public class GuardianshipRepositoryIntegrationTest {

    @Autowired
    private GuardianshipRepository guardianshipRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void mustPersistGuardianshipInDatabase() {
        UUID tutorId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();

        insertTutorGraph(tutorId);
        insertPet(petId);

        Guardianship guardianship = new Guardianship();
        guardianship.setCreated_at(LocalDateTime.now());
        guardianship.setTutor(entityManager.getEntityManager().getReference(Tutor.class, tutorId));
        guardianship.setPet(entityManager.getEntityManager().getReference(Pet.class, petId));

        Guardianship saved = guardianshipRepository.save(guardianship);
        entityManager.flush();
        entityManager.clear();

        Optional<Guardianship> fetched = guardianshipRepository.findById(saved.getId());

        Assertions.assertNotNull(saved.getId());
        Assertions.assertTrue(fetched.isPresent());
    }

    private void insertTutorGraph(UUID tutorId) {
        String providerSubject = "provider-" + tutorId;
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        entityManager.getEntityManager()
                .createNativeQuery("insert into providers (provider_subject, provider_name) values (?, ?)")
                .setParameter(1, providerSubject)
                .setParameter(2, "google")
                .executeUpdate();

        entityManager.getEntityManager()
                .createNativeQuery("""
                        insert into users (id, role, email, provider_id, created_at, updated_at)
                        values (?, 'tutor'::role_type, ?, ?, ?, ?)
                        """)
                .setParameter(1, userId)
                .setParameter(2, "user@example.com")
                .setParameter(3, providerSubject)
                .setParameter(4, now)
                .setParameter(5, now)
                .executeUpdate();

        entityManager.getEntityManager()
                .createNativeQuery("""
                        insert into tutors (
                            id, user_id, name, username, is_private, birthday, updated_at, reports_counter
                        )
                        values (?, ?, ?, ?, false, ?, ?, 0)
                        """)
                .setParameter(1, tutorId)
                .setParameter(2, userId)
                .setParameter(3, "Tutor Name")
                .setParameter(4, "tutor." + tutorId.toString().substring(0, 8))
                .setParameter(5, now.minusYears(20))
                .setParameter(6, now)
                .executeUpdate();
    }

    private void insertPet(UUID petId) {
        LocalDateTime now = LocalDateTime.now();

        entityManager.getEntityManager()
                .createNativeQuery("""
                        insert into pets (
                            id, name, username, img_url, birthday, species, created_at, updated_at
                        )
                        values (?, ?, ?, ?, ?, 'DOG'::species, ?, ?)
                        """)
                .setParameter(1, petId)
                .setParameter(2, "Dog Name")
                .setParameter(3, "dog." + petId.toString().substring(0, 8))
                .setParameter(4, "https://example.com/dog.png")
                .setParameter(5, now.minusYears(3))
                .setParameter(6, now)
                .setParameter(7, now)
                .executeUpdate();
    }
}
