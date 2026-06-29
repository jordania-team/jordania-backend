package com.jordania.api.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String provider,
        String role,
        String name
) {
    public static UserResponse from(Users user, String name) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getProvider().getProvider_name(),
                user.getRole().name(),
                name
        );
    }
}
