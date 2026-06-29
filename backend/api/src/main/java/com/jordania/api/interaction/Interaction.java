package com.jordania.api.interaction;
import lombok.*;
import com.jordania.api.post.Post;
import com.jordania.api.tutor.Tutor;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "interactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interactions_post_tutor",
                        columnNames = {"post_id", "tutor_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "interacted_at", nullable = false)
    private LocalDateTime interactedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        interactedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public LocalDateTime getInteractedAt() {
        return interactedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }
}