package com.jordania.api.Pet.PetDTO;

import java.sql.Date;


public record PetRequestDTO(String username, String name, Date birthday, String species, String img_url) {}
