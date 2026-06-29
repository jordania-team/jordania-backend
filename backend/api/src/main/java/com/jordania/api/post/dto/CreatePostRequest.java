package com.jordania.api.post.dto;

import java.util.List;
import java.util.UUID;

//record eh uma forma de escrever classe imutavel
//como eh dto, apenas carrega dado, nao muda ele
public record CreatePostRequest(
        UUID tutorId,
        List<UUID> petIds,
        String description
) {}

/* mesmo que
{
  "tutorId": "11111111-1111-1111-1111-111111111111",
  "petIds": [
    "22222222-2222-2222-2222-222222222222",
    "22222222-2222-2222-2222-222222222223"
  ],
  "description": "Passeio no parque"
}
* */