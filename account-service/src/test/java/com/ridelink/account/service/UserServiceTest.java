package com.ridelink.account.service;

import java.util.Optional;

import com.ridelink.account.entity.User;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void activeUserCanUpdateFullName() {
        User user = new User(
                "Old Name",
                "rider@example.com",
                "stored-password-hash",
                "RIDER"
        );
        user.setId("user-1");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateProfile(
                "user-1",
                "  Kamal Perera  "
        );

        assertEquals("Kamal Perera", updatedUser.getFullName());

        // Other account details must remain unchanged.
        assertEquals("user-1", updatedUser.getId());
        assertEquals("rider@example.com", updatedUser.getEmail());
        assertEquals("stored-password-hash", updatedUser.getPassword());
        assertEquals("RIDER", updatedUser.getRole());
        assertTrue(updatedUser.isActive());

        verify(userRepository).save(user);
    }

    @Test
    void inactiveUserCannotUpdateProfile() {
        User user = new User(
                "Old Name",
                "rider@example.com",
                "stored-password-hash",
                "RIDER"
        );
        user.setId("user-1");
        user.setActive(false);

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateProfile("user-1", "New Name")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Account is inactive", exception.getMessage());
        assertEquals("Old Name", user.getFullName());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void missingUserCannotUpdateProfile() {
        when(userRepository.findById("missing-user"))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateProfile(
                        "missing-user",
                        "New Name"
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Account no longer exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}