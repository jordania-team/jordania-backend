package com.jordania.api.Tutors;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TutorsRequest(
        @NotBlank
        String name,

        @NotBlank
        String username,

        @NotNull
        @JsonProperty("is_private")
        Boolean is_private,

        @JsonProperty("img_url")
        String img_url,

        @NotNull
        LocalDateTime birthday
) {
}
