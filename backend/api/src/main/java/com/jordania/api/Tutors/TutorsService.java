package com.jordania.api.Tutors;

import com.jordania.api.User.Tutors;
import com.jordania.api.User.TutorsRepository;
import com.jordania.api.User.Users;
import com.jordania.api.User.UsersRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TutorsService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._]{2,29}$");

    private final TutorsRepository tutorsRepository;
    private final UsersRepository usersRepository;

    public TutorsService(TutorsRepository tutorsRepository, UsersRepository usersRepository) {
        this.tutorsRepository = tutorsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public TutorsResponse findMe(UUID user_id) {
        Tutors tutors = tutorsRepository.findByUserId(user_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return TutorsResponse.from(tutors);
    }

    @Transactional
    public TutorsResponse saveMe(UUID user_id, TutorsRequest request) {
        Users user = usersRepository.findById(user_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String name = normalizedRequired(request.name(), "name");
        String username = normalizedUsername(request.username());
        LocalDateTime birthday = request.birthday();

        if (birthday.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "birthday must be in the past");
        }

        Tutors tutors = tutorsRepository.findByUserId(user_id).orElse(null);
        Tutors existingUsernameOwner = tutorsRepository.findByUsername(username).orElse(null);
        if (existingUsernameOwner != null
                && (tutors == null || !existingUsernameOwner.getId().equals(tutors.getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }

        if (tutors == null) {
            tutors = new Tutors(
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
                    request.img_url(),
                    birthday
            );
        }

        return TutorsResponse.from(tutorsRepository.save(tutors));
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
