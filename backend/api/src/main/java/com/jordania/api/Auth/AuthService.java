package com.jordania.api.Auth;

import com.jordania.api.User.Usuario;
import com.jordania.api.User.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final SocialTokenVerifier tokenVerifier;
    private final UsuarioRepository usuarioRepository;
    private final InternalTokenService internalTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            SocialTokenVerifier tokenVerifier,
            UsuarioRepository usuarioRepository,
            InternalTokenService internalTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.tokenVerifier        = tokenVerifier;
        this.usuarioRepository    = usuarioRepository;
        this.internalTokenService = internalTokenService;
        this.refreshTokenService  = refreshTokenService;
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

        String accessToken = internalTokenService.issue(saved);
        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(saved);

        return new LoginResponse(
                accessToken,
                saved.getId(),
                saved.getNome(),
                saved.getEmail(),
                refresh.raw(),
                refresh.expiresAt()
        );
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken old    = refreshTokenService.consume(rawRefreshToken);
        Usuario usuario     = old.getUser();

        String accessToken  = internalTokenService.issue(usuario);
        RefreshTokenService.IssuedRefreshToken newRefresh =
                refreshTokenService.issue(usuario, old.getFamilyId());

        // Marca o token antigo como substituido (reuse detection)
        old.replacedBy(refreshTokenService.sha256(newRefresh.raw()));

        return new LoginResponse(
                accessToken,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                newRefresh.raw(),
                newRefresh.expiresAt()
        );
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }
}
