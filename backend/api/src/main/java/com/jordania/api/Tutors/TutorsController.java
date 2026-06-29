package com.jordania.api.Tutors;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/tutors")
public class TutorsController {

    private final TutorsService tutorsService;

    public TutorsController(TutorsService tutorsService) {
        this.tutorsService = tutorsService;
    }

    @GetMapping("/me")
    public TutorsResponse me(@AuthenticationPrincipal Jwt jwt) {
        return tutorsService.findMe(userId(jwt));
    }

    @PutMapping("/me")
    public TutorsResponse saveMe(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid TutorsRequest request
    ) {
        return tutorsService.saveMe(userId(jwt), request);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TutorsResponse uploadProfileImage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file
    ) {
        return tutorsService.uploadProfileImage(userId(jwt), file);
    }

    @DeleteMapping("/me/profile-image")
    public TutorsResponse deleteProfileImage(@AuthenticationPrincipal Jwt jwt) {
        return tutorsService.deleteProfileImage(userId(jwt));
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
