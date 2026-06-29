package com.jordania.api.postpet;

import com.jordania.api.pet.Pet;
import com.jordania.api.post.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "post_pets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_pets_pet_post",
                        columnNames = {"pet_id", "post_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostPet {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public UUID getId() {
        return id;
    }

    public Pet getPet() {
        return pet;
    }

    public Post getPost() {
        return post;
    }
}