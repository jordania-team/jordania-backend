package com.jordania.api.Tutors;

import com.jordania.api.Auth.AuthController;
import com.jordania.api.Auth.AuthService;
import com.jordania.api.Auth.InternalTokenService;
import com.jordania.api.Auth.LoginResponse;
import com.jordania.api.Auth.SecurityConfig;
import com.jordania.api.User.Providers;
import com.jordania.api.User.RoleType;
import com.jordania.api.User.UserController;
import com.jordania.api.User.UserResponse;
import com.jordania.api.User.UserService;
import com.jordania.api.User.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TutorsController.class, AuthController.class, UserController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.auth.jwt-secret=test-secret-with-at-least-thirty-two-bytes",
        "app.auth.issuer=pocapi"
})
class TutorsControllerSecurityTest {

    private static final String ISSUER = "pocapi";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private TutorsService tutorsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Test
    void tokenIssuedByInternalTokenServiceIsAcceptedAndReachesService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.findMe(eq(userId))).thenReturn(tutorResponse(userId));

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalServiceToken(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    void tamperedTokenIsRejectedWith401() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = token(userId, ISSUER, "tutor", validExpiry());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejectedWith401() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = token(userId, ISSUER, "tutor", Instant.now().minusSeconds(3600));

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongIssuerTokenIsRejectedWith401() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = token(userId, "another-issuer", "tutor", validExpiry());

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingAuthorizationHeaderIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/tutors/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tutorRoleCanAccessTutorEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.findMe(eq(userId))).thenReturn(tutorResponse(userId));

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isOk());
    }

    @Test
    void adminRoleCannotAccessTutorEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "admin", validExpiry())))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenWithoutRoleCannotAccessTutorEndpoint() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, null, validExpiry())))
                .andExpect(status().isForbidden());
    }

    @Test
    void validTokenWithoutTutorReturns404NotAuthError() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.findMe(eq(userId)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isNotFound());
    }

    @Test
    void putCreatesOrUpdatesTutorWithValidToken() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.saveMe(eq(userId), any(TutorsRequest.class))).thenReturn(tutorResponse(userId));

        String body = """
                {
                  "name": "Taylor",
                  "username": "taylor",
                  "is_private": false,
                  "img_url": null,
                  "birthday": "1990-01-01T00:00:00"
                }
                """;

        mockMvc.perform(put("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    void putWithInvalidBodyReturns400() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put("/api/tutors/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postProfileImageWithValidTokenReachesService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.uploadProfileImage(eq(userId), any(MultipartFile.class)))
                .thenReturn(tutorResponse(userId));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/tutors/me/profile-image")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    void deleteProfileImageWithValidTokenReachesService() throws Exception {
        UUID userId = UUID.randomUUID();
        when(tutorsService.deleteProfileImage(eq(userId))).thenReturn(tutorResponse(userId));

        mockMvc.perform(delete("/api/tutors/me/profile-image")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()));
    }

    @Test
    void loginEndpointStaysPublic() throws Exception {
        when(authService.login(any())).thenReturn(
                loginResponse("internal-jwt", UUID.randomUUID()));

        String body = """
                {
                  "provider": "google",
                  "identityToken": "identity-token"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("internal-jwt"));
    }

    @Test
    void refreshEndpointStaysPublic() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.refresh("refresh-token")).thenReturn(loginResponse("new-internal-jwt", userId));

        String body = """
                {
                  "refreshToken": "refresh-token"
                }
                """;

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-internal-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithValidTokenRevokesRefreshTokens() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isNoContent());

        verify(authService).logout(userId);
    }

    @Test
    void usersMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersMeReturnsCurrentSession() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getMe(userId))
                .thenReturn(new UserResponse(userId, "taylor@example.com", "google", "tutor", "Taylor"));

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(userId, ISSUER, "tutor", validExpiry())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("taylor@example.com"))
                .andExpect(jsonPath("$.provider").value("google"))
                .andExpect(jsonPath("$.role").value("tutor"))
                .andExpect(jsonPath("$.name").value("Taylor"));
    }

    @Test
    void actuatorHealthIsNotBlockedBySecurity() throws Exception {
        int statusCode = mockMvc.perform(get("/actuator/health"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(statusCode).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(statusCode).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private Instant validExpiry() {
        return Instant.now().plusSeconds(3600);
    }

    private String internalServiceToken(UUID userId) {
        Users user = new Users(
                userId,
                RoleType.tutor,
                new Providers("google-subject", "google"),
                "taylor@example.com",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        InternalTokenService tokenService = new InternalTokenService(jwtEncoder, ISSUER, Duration.ofHours(1));
        return tokenService.issue(user, "Taylor");
    }

    private String token(UUID userId, String issuer, String role, Instant expiry) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(expiry.minusSeconds(3600))
                .expiresAt(expiry)
                .subject(userId.toString());
        if (role != null) {
            claims.claim("role", role);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    private LoginResponse loginResponse(String token, UUID userId) {
        return new LoginResponse(
                token,
                userId,
                "Taylor",
                "taylor@example.com",
                "tutor",
                "refresh-token-value",
                Instant.parse("2026-07-26T12:00:00Z")
        );
    }

    private TutorsResponse tutorResponse(UUID userId) {
        return new TutorsResponse(
                UUID.randomUUID(),
                userId,
                "Taylor",
                "taylor",
                false,
                null,
                LocalDateTime.of(1990, 1, 1, 0, 0),
                LocalDateTime.now(),
                0
        );
    }
}
