package com.rodrigoborges.pocapi.auth;

public record SocialIdentity(
        AuthProvider provider,
        String subject,
        String name,
        String email
) {
}
