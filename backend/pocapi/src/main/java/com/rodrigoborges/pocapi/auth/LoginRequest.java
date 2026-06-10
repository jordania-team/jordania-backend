package com.rodrigoborges.pocapi.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String provider,
        @NotBlank String identityToken,
        String name,
        String rawNonce
) {
}
