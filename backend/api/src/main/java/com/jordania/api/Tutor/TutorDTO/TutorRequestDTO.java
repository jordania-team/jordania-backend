package com.jordania.api.Tutor.TutorDTO;

import java.sql.Date;

public record TutorRequestDTO(String name, String username, Boolean is_private, String img_url, Date birthday, Date updated_at, int reports_counter) {}
        // @NotNull(message = "O título deve ser informado") String title,
        // @NotNull(message = "A descrição deve ser informada") String description,
        // @NotNull(message = "A data deve ser informada") Long date, String city,
        // @NotNull(message = "O estado deve ser informado") String state,
        // Boolean remote,
        // @Pattern(regexp = "^(https?://)(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,63}){1,3}(/[a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=-]*)?$", message = "URL inválida")
        // @NotEmpty(message = "O link para o evento deve ser informado") String eventUrl,
        // MultipartFile image) {

