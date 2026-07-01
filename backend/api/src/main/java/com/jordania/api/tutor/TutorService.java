package com.jordania.api.tutor;

import com.jordania.api.Storage.ImageStorageService;
import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;
import com.jordania.api.tutor.dto.*;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TutorService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._]{2,29}$");

    private final TutorRepository tutorsRepository;
    private final UsersRepository usersRepository;
    private final ImageStorageService imageStorageService;

    public TutorService(
            TutorRepository tutorsRepository,
            UsersRepository usersRepository,
            ImageStorageService imageStorageService
    ) {
        this.tutorsRepository = tutorsRepository;
        this.usersRepository = usersRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public TutorResponse findMe(UUID user_id) {
        return response(tutorByUserId(user_id));
    }

    @Transactional
    public TutorResponse saveMe(UUID user_id, TutorRequest request) {
        Users user = usersRepository.findById(user_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String name = normalizedRequired(request.name(), "name");
        String username = normalizedUsername(request.username());
        LocalDateTime birthday = request.birthday();

        if (birthday.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "birthday must be in the past");
        }

        Tutor tutors = tutorsRepository.findByUserId(user_id).orElse(null);
        Tutor existingUsernameOwner = tutorsRepository.findByUsername(username).orElse(null);
        if (existingUsernameOwner != null
                && (tutors == null || !existingUsernameOwner.getId().equals(tutors.getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }

        if (tutors == null) {
            tutors = new Tutor(
                    user,
                    name,
                    username,
                    request.is_private(),
                    request.img_url(),
                    birthday
            );
        } else {
            tutors.update(
                    name,
                    username,
                    request.is_private(),
                    imageForUpdate(tutors, request.img_url()),
                    birthday
            );
        }

        return response(tutorsRepository.save(tutors));
    }

    @Transactional
    public TutorResponse uploadProfileImage(UUID user_id, MultipartFile file) {
        Tutor tutors = tutorByUserId(user_id);
        String previousImage = tutors.getImg_url();
        String imageKey = imageStorageService.uploadTutorProfileImage(tutors.getId(), file);

        tutors.update(
                tutors.getName(),
                tutors.getUsername(),
                tutors.isIs_private(),
                imageKey,
                tutors.getBirthday()
        );

        Tutor saved = tutorsRepository.save(tutors);
        if (previousImage != null && !previousImage.isBlank() && !imageKey.equals(previousImage)) {
            imageStorageService.deleteIfStoredObject(previousImage);
        }
        return response(saved);
    }

    @Transactional
    public TutorResponse deleteProfileImage(UUID user_id) {
        Tutor tutors = tutorByUserId(user_id);
        String storedImage = tutors.getImg_url();
        if (storedImage == null || storedImage.isBlank()) {
            return response(tutors);
        }

        tutors.update(
                tutors.getName(),
                tutors.getUsername(),
                tutors.isIs_private(),
                null,
                tutors.getBirthday()
        );

        Tutor saved = tutorsRepository.save(tutors);
        imageStorageService.deleteIfStoredObject(storedImage);
        return response(saved);
    }

    private Tutor tutorByUserId(UUID user_id) {
        return tutorsRepository.findByUserId(user_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private TutorResponse response(Tutor tutors) {
        return TutorResponse.from(tutors, imageStorageService::displayUrl);
    }

    private String imageForUpdate(Tutor tutors, String requestedImage) {
        if (requestedImage == null || requestedImage.isBlank()) {
            return tutors.getImg_url();
        }
        if (imageStorageService.isDisplayUrlForStoredObject(requestedImage, tutors.getImg_url())) {
            return tutors.getImg_url();
        }
        return requestedImage;
    }

    private String normalizedRequired(String value, String field) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return normalized;
    }

    private String normalizedUsername(String username) {
        String normalized = normalizedRequired(username, "username").toLowerCase(Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is invalid");
        }
        return normalized;
    }
}
