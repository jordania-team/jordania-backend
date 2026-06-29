package com.jordania.api.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponse (
        UUID id,

        UUID tutorId,
        String tutorUsername,

        String description,

        List<UUID> petIds,

        LocalDateTime postedAt,

        Integer counter1,
        Integer counter2,
        Integer counter3,
        Integer counter4,
        Integer counter5
) {}

/*usa pra montar feed dps
para poca maiores, dividir entre TutorSummaryResponse e List<PetSummaryResponse>
{
  "id": "44444444-4444-4444-4444-444444444444",
  "tutorId": "11111111-1111-1111-1111-111111111111",
  "tutorUsername": "gabi",
  "description": "Passeio no parque!",
  "petIds": [
    "22222222-2222-2222-2222-222222222222",
    "22222222-2222-2222-2222-222222222223"
  ],
  "postedAt": "2026-06-25T14:15:00",
  "counter1": 10,
  "counter2": 3,
  "counter3": 0,
  "counter4": 1,
  "counter5": 0
}
 */