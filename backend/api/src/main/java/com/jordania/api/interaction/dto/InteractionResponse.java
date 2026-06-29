package com.jordania.api.interaction.dto;

import com.jordania.api.interaction.InteractionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InteractionResponse(
        UUID id,
        UUID postId,
        UUID tutorId,
        InteractionType interactionType,
        LocalDateTime interactedAt,
        LocalDateTime updatedAt
) {}