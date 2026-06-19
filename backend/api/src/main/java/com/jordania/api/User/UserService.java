package com.jordania.api.User;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UsuarioRepository usuarioRepository;

    public UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /// busca o usuario pelo ID extraido do JWT
    /// lanca IllegalArgumentException se o usuario nao for encontrado —
    /// o controller mapeia isso para 404
    public UserResponse getById(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
        return UserResponse.from(usuario);
    }
}
