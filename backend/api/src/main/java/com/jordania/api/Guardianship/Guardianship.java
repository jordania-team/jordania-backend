package com.jordania.api.Guardianship;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jordania.api.Pet.Pet;
import com.jordania.api.Tutor.Tutor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Guardianships")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Guardianship {
    @Id
    @GeneratedValue
    private UUID id;

    private LocalDateTime created_at;

    @OneToOne
    @JoinColumn(name= "pet_id")
    private Pet pet;

    @OneToOne
    @JoinColumn(name= "tutor_id")
    private Tutor tutor;

}
