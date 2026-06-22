package com.jordania.api.Tutor;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Tutors")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tutor {
    @Id
    @GeneratedValue
    private UUID id;
    
    private UUID user_id;
    private String name;
    private String username;
    private Boolean is_private;
    private String img_url;

}
