package com.jordania.api.interaction.dto;

import com.jordania.api.interaction.InteractionType;

import java.util.UUID;

public record CreateInteractionRequest(
        UUID postId,
        UUID tutorId,
        InteractionType interactionType
) {}