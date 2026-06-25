package com.jordania.api.Tutor.TutorDTO;

import java.sql.Date;

public record TutorResponseDTO(String name, String username, Boolean is_private, String img_url, Date birthday) {
    
}
