package com.jordania.api.Auth;

import com.jordania.api.User.Providers;
import com.jordania.api.User.ProvidersRepository;
import com.jordania.api.User.TutorsRepository;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final SocialTokenVerifier tokenVerifier;
    private final ProvidersRepository providersRepository;
    private final UsersRepository usersRepository;
    private final TutorsRepository tutorsRepository;
    private final InternalTokenService internalTokenService;

    public AuthService(
            SocialTokenVerifier tokenVerifier,
            ProvidersRepository providersRepository,
            UsersRepository usersRepository,
            TutorsRepository tutorsRepository,
            InternalTokenService internalTokenService
    ) {
        this.tokenVerifier = tokenVerifier;
        this.providersRepository = providersRepository;
        this.usersRepository = usersRepository;
        this.tutorsRepository = tutorsRepository;
        this.internalTokenService = internalTokenService;
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

        return new LoginResponse(
                internalTokenService.issue(savedUsers, responseName),
                savedUsers.getId(),
                responseName,
                savedUsers.getEmail(),
                savedUsers.getRole().name()
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
