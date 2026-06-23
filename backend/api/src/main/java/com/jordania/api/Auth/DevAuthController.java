package com.jordania.api.Auth;

import com.jordania.api.User.Usuario;
import com.jordania.api.User.UsuarioRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/// Endpoint exclusivo para ambiente local (profile "local").
/// Emite um par (access + refresh) válido sem depender de Apple/Google,
/// permitindo testar endpoints protegidos e o fluxo de refresh localmente.
/// NUNCA sobe para produção — @Profile("local") garante que este controller
/// não é registrado na AWS.
@RestController
@RequestMapping("/dev")
@Profile("local")
public class DevAuthController {

    private final UsuarioRepository usuarioRepository;
    private final InternalTokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public DevAuthController(
            UsuarioRepository usuarioRepository,
            InternalTokenService tokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.usuarioRepository   = usuarioRepository;
        this.tokenService        = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public LoginResponse devLogin(@RequestParam UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        String accessToken = tokenService.issue(usuario);
        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(usuario);

        return new LoginResponse(
                accessToken,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                refresh.raw(),
                refresh.expiresAt()
        );
    }
}
