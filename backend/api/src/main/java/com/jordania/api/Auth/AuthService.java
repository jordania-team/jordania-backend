package com.jordania.api.Auth;

import com.jordania.api.User.Providers;
import com.jordania.api.User.ProvidersRepository;
import com.jordania.api.tutor.TutorRepository;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final SocialTokenVerifier tokenVerifier;
    private final ProvidersRepository providersRepository;
    private final UsersRepository usersRepository;
    private final TutorRepository tutorsRepository;
    private final InternalTokenService internalTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            SocialTokenVerifier tokenVerifier,
            ProvidersRepository providersRepository,
            UsersRepository usersRepository,
            TutorRepository tutorsRepository,
            InternalTokenService internalTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.tokenVerifier = tokenVerifier;
        this.providersRepository = providersRepository;
        this.usersRepository = usersRepository;
        this.tutorsRepository = tutorsRepository;
        this.internalTokenService = internalTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        SocialIdentity identity = tokenVerifier.verify(request);
        String providerName = providerName(identity.provider());

        Providers providers = providersRepository.findById(identity.subject())
                .map(existing -> requireSameProvider(existing, providerName))
                .orElseGet(() -> providersRepository.save(new Providers(
                        identity.subject(),
                        providerName
                )));

        Users users = usersRepository
                .findByProviderProviderSubject(providers.getProvider_subject())
                .orElseGet(() -> new Users(providers, identity.email()));
        users.updateEmail(identity.email());
        Users savedUsers = usersRepository.save(users);

        String responseName = tutorsRepository.findByUserId(savedUsers.getId())
                .map(tutors -> tutors.getName())
                .orElse(identity.name());

        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(savedUsers);

        return response(savedUsers, responseName, refreshToken);
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken oldRefreshToken = refreshTokenService.consume(rawRefreshToken);
        Users user = oldRefreshToken.getUser();

        String responseName = tutorsRepository.findByUserId(user.getId())
                .map(tutors -> tutors.getName())
                .orElse(null);
        RefreshTokenService.IssuedRefreshToken newRefreshToken =
                refreshTokenService.issue(user, oldRefreshToken.getFamilyId());
        oldRefreshToken.replacedBy(refreshTokenService.sha256(newRefreshToken.raw()));

        return response(user, responseName, newRefreshToken);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private LoginResponse response(
            Users user,
            String name,
            RefreshTokenService.IssuedRefreshToken refreshToken
    ) {
        return new LoginResponse(
                internalTokenService.issue(user, name),
                user.getId(),
                name,
                user.getEmail(),
                user.getRole().name(),
                refreshToken.raw(),
                refreshToken.expiresAt()
        );
    }

    private Providers requireSameProvider(Providers providers, String providerName) {
        if (!providers.getProvider_name().equals(providerName)) {
            throw new InvalidIdentityTokenException("Provider subject already belongs to another provider");
        }
        return providers;
    }

    private String providerName(AuthProvider provider) {
        return provider.name().toLowerCase(Locale.ROOT);
    }
}
