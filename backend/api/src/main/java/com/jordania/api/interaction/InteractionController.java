package com.jordania.api.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.jordania.api.interaction.dto.CreateInteractionRequest;
import com.jordania.api.interaction.dto.InteractionResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    public InteractionResponse createOrUpdateInteraction(
            @RequestBody CreateInteractionRequest request
    ) {
        return interactionService.createOrUpdateInteraction(request);
    }

    @GetMapping("/post/{postId}")
    public List<InteractionResponse> getInteractionsByPost(
            @PathVariable UUID postId
    ) {
        return interactionService.getInteractionsByPost(postId);
    }

    @GetMapping("/tutor/{tutorId}")
    public List<InteractionResponse> getInteractionsByTutor(
            @PathVariable UUID tutorId
    ) {
        return interactionService.getInteractionsByTutor(tutorId);
    }
}