package com.jordania.api.User;

import com.jordania.api.Auth.AuthProvider;

import java.util.UUID;

/// DTO de resposta para GET /users/me.
/// Expõe apenas os campos necessários para o cliente validar e atualizar a sessão local.
public record UserResponse(
        UUID id,
        String name,
        String email,
        AuthProvider provider
) {
    public static UserResponse from(Usuario usuario) {
        return new UserResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getProvider()
        );
    }
}
