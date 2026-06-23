package com.rodrigoborges.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jordania.api.Tutor.TutorRepository;
import com.jordania.api.Tutor.TutorService;

@ExtendWith(MockitoExtension.class)
public class TutorServiceTest {
    // unit test code

    @Mock
    private TutorRepository tutorRepository;

    @InjectMocks
    private TutorService tutorService; // specifies to only test service

    @Test
    void shouldReturnTutorById() {
        // Arrange
        // Tutor mockTutor = new Tutor(1L, "John");
        // when(tutorRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // // Act
        // User result = userService.getUserById(1L);

        // // Assert
        // assertEquals("John", result.getName());
    }


}
