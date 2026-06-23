package com.jordania.api.Tutor;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorRepository repository;
    private final TutorService service;

    @PostMapping("/tutors")
    Tutor newTutor(@RequestBody Tutor newTutor) {
        return repository.save(newTutor);
    }

    @DeleteMapping("/employees/{id}")
    void deleteTutor(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    // @GetMapping
    // public ResponseEntity<List<TutorResponseDTO>> getAllTutors(){
    //     return ResponseEntity.ok(service.findAll());
    // }
    
    // TODO: a implementar
}
