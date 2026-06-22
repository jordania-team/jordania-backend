package com.jordania.api.Auth;

import com.jordania.api.User.Usuario;
import com.jordania.api.User.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/// endpoint exclusivo para ambiente local (profile "local")
/// permite emitir um JWT valido sem depender de Apple/Google — sendo usado para testar endpoints protegidos
/// NUNCA sobe para producao — @Profile("local") garante que este controller nao é registrado na AWS.
@RestController
@RequestMapping("/dev")
@Profile("local")
public class DevAuthController {

    private final UsuarioRepository usuarioRepository;
    private final InternalTokenService tokenService;

    public DevAuthController(UsuarioRepository usuarioRepository, InternalTokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse devLogin(@RequestParam UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return new LoginResponse(
                tokenService.issue(usuario),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}