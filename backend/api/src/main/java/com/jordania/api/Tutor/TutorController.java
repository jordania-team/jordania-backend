package com.jordania.api.Tutor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorRepository repository;
    private final TutorService service;

    // @PostMapping("/tutors")
    // Tutor newTutor(@RequestBody Tutor newTutor) {
    //     return repository.insertQuery(newTutor);
    // }

    // @DeleteMapping("/employees/{id}")
    // void deleteTutor(@PathVariable UUID id) {
    //     repository.deleteById(id);
    // }

    // @GetMapping
    // public ResponseEntity<List<TutorResponseDTO>> getAllTutors(){
    //     return ResponseEntity.ok(service.findAll());
    // }
    
    // TODO: a implementar
}
