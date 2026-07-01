package com.jordania.api.User;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import com.jordania.api.tutor.*;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final TutorRepository tutorsRepository;

    public UserService(UsersRepository usersRepository, TutorRepository tutorsRepository) {
        this.usersRepository = usersRepository;
        this.tutorsRepository = tutorsRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        String name = tutorsRepository.findByUserId(userId)
                .map(Tutor::getName)
                .orElse(null);
        return UserResponse.from(user, name);
    }
}
