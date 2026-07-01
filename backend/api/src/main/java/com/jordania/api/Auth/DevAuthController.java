package com.jordania.api.Auth;

import com.jordania.api.tutor.TutorRepository;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/dev")
@Profile("local")
public class DevAuthController {

    private final UsersRepository usersRepository;
    private final TutorRepository tutorsRepository;
    private final InternalTokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    public DevAuthController(
            UsersRepository usersRepository,
            TutorRepository tutorsRepository,
            InternalTokenService tokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.usersRepository = usersRepository;
        this.tutorsRepository = tutorsRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public LoginResponse devLogin(@RequestParam UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        String name = tutorsRepository.findByUserId(userId)
                .map(tutors -> tutors.getName())
                .orElse(null);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);

        return new LoginResponse(
                tokenService.issue(user, name),
                user.getId(),
                name,
                user.getEmail(),
                user.getRole().name(),
                refreshToken.raw(),
                refreshToken.expiresAt()
        );
    }
}
