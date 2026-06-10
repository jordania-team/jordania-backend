package com.rodrigoborges.pocapi.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final SocialTokenVerifier tokenVerifier;
    private final UsuarioRepository usuarioRepository;
    private final InternalTokenService internalTokenService;

    public AuthService(
            SocialTokenVerifier tokenVerifier,
            UsuarioRepository usuarioRepository,
            InternalTokenService internalTokenService
    ) {
        this.tokenVerifier = tokenVerifier;
        this.usuarioRepository = usuarioRepository;
        this.internalTokenService = internalTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SocialIdentity identity = tokenVerifier.verify(request);

        Usuario usuario = usuarioRepository
                .findByProviderAndProviderSubject(identity.provider(), identity.subject())
                .orElseGet(() -> new Usuario(
                        identity.provider(),
                        identity.subject(),
                        identity.name(),
                        identity.email()
                ));

        usuario.updateProfile(identity.name(), identity.email());
        Usuario saved = usuarioRepository.save(usuario);

        return new LoginResponse(
                internalTokenService.issue(saved),
                saved.getId(),
                saved.getNome(),
                saved.getEmail()
        );
    }
}
