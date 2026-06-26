package com.jordania.api.Tutors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jordania.api.User.Tutors;

import java.time.LocalDateTime;
import java.util.UUID;

public record TutorsResponse(
        UUID id,

        @JsonProperty("user_id")
        UUID user_id,

        String name,
        String username,

        @JsonProperty("is_private")
        boolean is_private,

        @JsonProperty("img_url")
        String img_url,

        LocalDateTime birthday,

        @JsonProperty("updated_at")
        LocalDateTime updated_at,

        @JsonProperty("reports_counter")
        int reports_counter
) {
    public static TutorsResponse from(Tutors tutors) {
        return new TutorsResponse(
                tutors.getId(),
                tutors.getUser().getId(),
                tutors.getName(),
                tutors.getUsername(),
                tutors.isIs_private(),
                tutors.getImg_url(),
                tutors.getBirthday(),
                tutors.getUpdated_at(),
                tutors.getReports_counter()
        );
    }
}
