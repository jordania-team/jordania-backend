package com.jordania.api.Pet;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

enum Species { // TODO: arrumar
    DOG,
    CAT,
    FISH,
    BIRD,
    OTHER
}

@Entity
@Table(name= "Pets")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pet {
    @Id
    @GeneratedValue
    private UUID id;

    private String username;
    private String name;
    private Date birthday;
    private Species species;
    private String img_url;
    private Date created_at;
    private Date updated_at; 
}
