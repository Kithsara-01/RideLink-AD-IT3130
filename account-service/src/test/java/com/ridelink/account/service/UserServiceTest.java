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
    void activeUserCanUpdateFullNameAndTelephoneNumber() {
        User user = new User(
                "Old Name",
                "rider@example.com",
                "stored-password-hash",
                "RIDER");

        user.setId("user-1");
        user.setTelephoneNumber("0771234567");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateProfile(
                "user-1",
                "  Kamal Perera  ",
                "  +94771234567  ");

        assertEquals("Kamal Perera", updatedUser.getFullName());
        assertEquals("+94771234567", updatedUser.getTelephoneNumber());

        // Other account details must remain unchanged.
        assertEquals("user-1", updatedUser.getId());
        assertEquals("rider@example.com", updatedUser.getEmail());
        assertEquals("stored-password-hash", updatedUser.getPassword());
        assertEquals("RIDER", updatedUser.getRole());
        assertTrue(updatedUser.isActive());

        verify(userRepository).save(user);
    }

    @Test
    void omittingTelephoneNumberPreservesExistingNumber() {
        User user = new User(
                "Old Name",
                "rider@example.com",
                "stored-password-hash",
                "RIDER");

        user.setId("user-1");
        user.setTelephoneNumber("0771234567");

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateProfile(
                "user-1",
                "New Name",
                null);

        assertEquals("New Name", updatedUser.getFullName());
        assertEquals("0771234567", updatedUser.getTelephoneNumber());

        verify(userRepository).save(user);
    }

    @Test
    void legacyUserWithNullTelephoneNumberCanBeRetrieved() {
        User user = new User(
                "Legacy User",
                "legacy@example.com",
                "stored-password-hash",
                "RIDER");

        user.setId("legacy-user");

        when(userRepository.findById("legacy-user"))
                .thenReturn(Optional.of(user));

        User retrievedUser = userService.findActiveById("legacy-user");

        assertEquals("legacy-user", retrievedUser.getId());
        assertEquals("Legacy User", retrievedUser.getFullName());
        assertEquals("legacy@example.com", retrievedUser.getEmail());
        assertNull(retrievedUser.getTelephoneNumber());
        assertTrue(retrievedUser.isActive());
    }

    @Test
    void inactiveUserCannotUpdateProfile() {
        User user = new User(
                "Old Name",
                "rider@example.com",
                "stored-password-hash",
                "RIDER");

        user.setId("user-1");
        user.setActive(false);

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateProfile(
                        "user-1",
                        "New Name",
                        "0771234567"));

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
                        "New Name",
                        "0771234567"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Account no longer exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminCanChangeRiderRoleToDriver() {
        User admin = new User(
                "Admin",
                "admin@ridelink.com",
                "stored-password-hash",
                "ADMIN");

        admin.setId("admin-1");

        User rider = new User(
                "Kamal",
                "kamal@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("user-1");

        when(userRepository.findById("admin-1"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(rider));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateAccountRole(
                "admin-1",
                "user-1",
                "DRIVER");

        assertEquals("DRIVER", updatedUser.getRole());

        verify(userRepository).save(rider);
    }

    @Test
    void nonAdminCannotChangeAccountRole() {
        User rider = new User(
                "Rider",
                "rider@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("rider-1");

        when(userRepository.findById("rider-1"))
                .thenReturn(Optional.of(rider));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateAccountRole(
                        "rider-1",
                        "user-1",
                        "DRIVER"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(
                "Only an admin can change account roles",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminCannotSetInvalidAccountRole() {
        User admin = new User(
                "Admin",
                "admin@ridelink.com",
                "stored-password-hash",
                "ADMIN");

        admin.setId("admin-1");

        User rider = new User(
                "Kamal",
                "kamal@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("user-1");

        when(userRepository.findById("admin-1"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(rider));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateAccountRole(
                        "admin-1",
                        "user-1",
                        "MANAGER"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(
                "Role must be RIDER or DRIVER",
                exception.getMessage());

        assertEquals("RIDER", rider.getRole());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changingAccountRoleIncrementsTokenVersion() {
        User admin = new User(
                "Admin",
                "admin@ridelink.com",
                "stored-password-hash",
                "ADMIN");

        admin.setId("admin-1");

        User rider = new User(
                "Kamal",
                "kamal@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("user-1");
        rider.setTokenVersion(0);

        when(userRepository.findById("admin-1"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(rider));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateAccountRole(
                "admin-1",
                "user-1",
                "DRIVER");

        assertEquals("DRIVER", updatedUser.getRole());
        assertEquals(1, updatedUser.getTokenVersion());

        verify(userRepository).save(rider);
    }

    @Test
    void changingAccountStatusIncrementsTokenVersion() {
        User admin = new User(
                "Admin",
                "admin@ridelink.com",
                "stored-password-hash",
                "ADMIN");

        admin.setId("admin-1");

        User rider = new User(
                "Kamal",
                "kamal@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("user-1");
        rider.setTokenVersion(1);

        when(userRepository.findById("admin-1"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(rider));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateAccountStatus(
                "admin-1",
                "user-1",
                false);

        assertFalse(updatedUser.isActive());
        assertEquals(2, updatedUser.getTokenVersion());

        verify(userRepository).save(rider);
    }

    @Test
    void unchangedRoleDoesNotIncrementTokenVersion() {
        User admin = new User(
                "Admin",
                "admin@ridelink.com",
                "stored-password-hash",
                "ADMIN");

        admin.setId("admin-1");

        User rider = new User(
                "Kamal",
                "kamal@example.com",
                "stored-password-hash",
                "RIDER");

        rider.setId("user-1");
        rider.setTokenVersion(2);

        when(userRepository.findById("admin-1"))
                .thenReturn(Optional.of(admin));

        when(userRepository.findById("user-1"))
                .thenReturn(Optional.of(rider));

        User unchangedUser = userService.updateAccountRole(
                "admin-1",
                "user-1",
                "RIDER");

        assertEquals(2, unchangedUser.getTokenVersion());

        verify(userRepository, never()).save(any(User.class));
    }
}