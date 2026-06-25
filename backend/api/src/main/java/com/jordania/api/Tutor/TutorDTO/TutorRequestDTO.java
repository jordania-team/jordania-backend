package com.jordania.api.Tutor.TutorDTO;

import java.sql.Date;

// DTO request only contains infos the user can access
public record TutorRequestDTO(String name, String username, Boolean is_private, String img_url, Date birthday) {}

