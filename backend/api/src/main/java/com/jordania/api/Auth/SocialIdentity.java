package com.jordania.api.Auth;

public record SocialIdentity(
        AuthProvider provider,
        String subject,
        String name,
        String email
) {
}
