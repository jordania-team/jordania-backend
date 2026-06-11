package com.jordania.api.Auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @ExceptionHandler(InvalidIdentityTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> invalidIdentityToken(InvalidIdentityTokenException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(AuthConfigurationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> missingConfiguration(AuthConfigurationException exception) {
        return Map.of("error", exception.getMessage());
    }
}
