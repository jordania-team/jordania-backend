package com.rodrigoborges.pocapi.auth;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        String name,
        String email
) {
}
