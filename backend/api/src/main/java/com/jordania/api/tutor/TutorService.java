package com.jordania.api.tutor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jordania.api.tutor.dto.TutorRequestDTO;

@Service
public class TutorService {

    private final TutorRepository repository;

    public TutorService(TutorRepository repository) {
        this.repository = repository;
    }


    public Tutor createTutor(TutorRequestDTO data) {
        /*  Creates a new instance of a Tutor
        Gets data from DTO, and sets it to a new tutor instance
        */

        // TODO: verify if user exists (check by username with repository)

        // tutor instance in memory
        Tutor tutor = new Tutor();
        tutor.setName(data.name());
        tutor.setUsername(data.username());
        tutor.setIsPrivate(data.is_private());
        tutor.setBirthday(data.birthday());
        tutor.setImg(data.img_url());

        
        // protected data not on dto
        tutor.setId(UUID.randomUUID());
        tutor.setUpdatedAt(LocalDateTime.now());
        tutor.setCreatedAt(LocalDateTime.now());
        tutor.setReportsCounter(0);

        // inserts in the database
        repository.insertQuery(tutor);
        return tutor;
    }

    public Tutor findTutorById(UUID id) {
        return repository.searchByIdQuery(id)
                    .orElseThrow(() -> new RuntimeException("Tutor not found with ID:" + id));
    }

}
