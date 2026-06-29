package com.jordania.api.interaction;
import com.jordania.api.interaction.dto.InteractionResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jordania.api.interaction.dto.CreateInteractionRequest;
import com.jordania.api.interaction.dto.InteractionResponse;
import com.jordania.api.post.Post;
import com.jordania.api.post.PostRepository;
import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final PostRepository postRepository;
    private final TutorRepository tutorRepository;

    public InteractionResponse createOrUpdateInteraction(CreateInteractionRequest request) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        Tutor tutor = tutorRepository.findById(request.tutorId())
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));

        Interaction interaction = interactionRepository
                .findByPostIdAndTutorId(request.postId(), request.tutorId())
                .orElse(null);

        if (interaction == null) {
            interaction = Interaction.builder()
                    .id(UUID.randomUUID())
                    .post(post)
                    .tutor(tutor)
                    .interactionType(request.interactionType())
                    .build();

            incrementCounter(post, request.interactionType());
        } else {
            InteractionType oldType = interaction.getInteractionType();
            InteractionType newType = request.interactionType();

            if (oldType != newType) {
                decrementCounter(post, oldType);
                incrementCounter(post, newType);
                interaction.setInteractionType(newType);
            }
        }

        Post savedPost = postRepository.save(post);
        Interaction savedInteraction = interactionRepository.save(interaction);

        return toResponse(savedInteraction);
    }

    public List<InteractionResponse> getInteractionsByPost(UUID postId) {
        return interactionRepository.findByPostId(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InteractionResponse> getInteractionsByTutor(UUID tutorId) {
        return interactionRepository.findByTutorId(tutorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void incrementCounter(Post post, InteractionType type) {
        switch (type) {
            case REACTION_1 -> post.setCounter1(post.getCounter1() + 1);
            case REACTION_2 -> post.setCounter2(post.getCounter2() + 1);
            case REACTION_3 -> post.setCounter3(post.getCounter3() + 1);
            case REACTION_4 -> post.setCounter4(post.getCounter4() + 1);
            case REACTION_5 -> post.setCounter5(post.getCounter5() + 1);
        }
    }

    private void decrementCounter(Post post, InteractionType type) {
        switch (type) {
            case REACTION_1 -> post.setCounter1(Math.max(0, post.getCounter1() - 1));
            case REACTION_2 -> post.setCounter2(Math.max(0, post.getCounter2() - 1));
            case REACTION_3 -> post.setCounter3(Math.max(0, post.getCounter3() - 1));
            case REACTION_4 -> post.setCounter4(Math.max(0, post.getCounter4() - 1));
            case REACTION_5 -> post.setCounter5(Math.max(0, post.getCounter5() - 1));
        }
    }

    private InteractionResponse toResponse(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getPost().getId(),
                interaction.getTutor().getId(),
                interaction.getInteractionType(),
                interaction.getInteractedAt(),
                interaction.getUpdatedAt()
        );
    }
}