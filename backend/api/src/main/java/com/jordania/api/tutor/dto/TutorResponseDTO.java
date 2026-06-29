package com.jordania.api.tutor.dto;

import java.time.LocalDateTime;

public record TutorResponseDTO(String name, String username, Boolean is_private, String img_url, LocalDateTime birthday) {
    
}
