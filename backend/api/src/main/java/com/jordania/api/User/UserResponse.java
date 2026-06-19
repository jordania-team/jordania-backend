package com.jordania.api.User;

import com.jordania.api.Auth.AuthProvider;

import java.util.UUID;

/// DTO de resposta para GET /users/me
/// expoe apenas os campos necessarios para o cliente validar e atualizar a sessao local
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
