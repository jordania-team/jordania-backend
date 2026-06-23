package com.jordania.api.Auth;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        String  token,
        UUID    userId,
        String  name,
        String  email,
        String  refreshToken,
        Instant refreshExpiresAt
) {
}
