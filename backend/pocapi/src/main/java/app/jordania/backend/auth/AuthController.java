package app.jordania.backend.auth;

import app.jordania.backend.security.JwtService;
import app.jordania.backend.user.User;
import app.jordania.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AppleTokenValidator appleTokenValidator;
    private final GoogleTokenValidator googleTokenValidator;

    public AuthController(
            JwtService jwtService,
            UserRepository userRepository,
            AppleTokenValidator appleTokenValidator,
            GoogleTokenValidator googleTokenValidator
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.appleTokenValidator = appleTokenValidator;
        this.googleTokenValidator = googleTokenValidator;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String providerId;
            String email;
            String name;

            switch (request.provider().toLowerCase()) {
                case "apple" -> {
                    var claims = appleTokenValidator.validate(request.token());
                    providerId = claims.sub();
                    email = claims.email() != null ? claims.email() : "";
                    name = request.name() != null ? request.name() : "";
                }
                case "google" -> {
                    var claims = googleTokenValidator.validate(request.token());
                    providerId = claims.sub();
                    email = claims.email() != null ? claims.email() : "";
                    name = claims.name() != null ? claims.name() : "";
                }
                default -> {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Unsupported provider: " + request.provider()));
                }
            }

            User user = userRepository.findByProviderId(providerId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setProvider(request.provider().toLowerCase());
                        newUser.setProviderId(providerId);
                        newUser.setName(name);
                        return userRepository.save(newUser);
                    });

            String token = jwtService.generateToken(user.getId(), user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "userId", user.getId(),
                    "name", user.getName() != null ? user.getName() : "",
                    "email", user.getEmail()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    public record LoginRequest(String provider, String token, String name) {}
}