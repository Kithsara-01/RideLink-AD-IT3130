package com.ridelink.account.controller;

import java.util.Map;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.entity.User;
import com.ridelink.account.service.JwtService;
import com.ridelink.account.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.ridelink.account.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

        private final UserService userService;
        private final JwtService jwtService;

        public AccountController(
                        UserService userService,
                        JwtService jwtService) {
                this.userService = userService;
                this.jwtService = jwtService;
        }

        @PostMapping("/register")
        public ResponseEntity<?> register(
                        @Valid @RequestBody RegisterRequest request) {

                User user = userService.register(request);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                Map.of(
                                                "message", "Account registered successfully",
                                                "id", user.getId(),
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "role", user.getRole(),
                                                "active", user.isActive()));
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(
                        @Valid @RequestBody LoginRequest request) {

                User user = userService.authenticate(
                                request.getEmail(),
                                request.getPassword());

                String token = jwtService.generateToken(user);

                return ResponseEntity.ok()
                                .header("Cache-Control", "no-store")
                                .header("Pragma", "no-cache")
                                .body(
                                                Map.of(
                                                                "message", "Login successful",
                                                                "accessToken", token,
                                                                "tokenType", "Bearer",
                                                                "expiresIn", jwtService.getExpirationSeconds(),
                                                                "id", user.getId(),
                                                                "fullName", user.getFullName(),
                                                                "email", user.getEmail(),
                                                                "role", user.getRole(),
                                                                "active", user.isActive()));
        }

        @GetMapping("/me")
        public ResponseEntity<?> getCurrentAccount(
                        @AuthenticationPrincipal Jwt jwt) {

                User user = userService.findActiveById(jwt.getSubject());

                return ResponseEntity.ok(
                                Map.of(
                                                "message", "Account retrieved successfully",
                                                "id", user.getId(),
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "role", user.getRole(),
                                                "active", user.isActive()));
        }

        @PatchMapping("/me")
        public ResponseEntity<?> updateProfile(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody UpdateProfileRequest request) {

                User user = userService.updateProfile(
                                jwt.getSubject(),
                                request.getFullName());

                return ResponseEntity.ok(
                                Map.of(
                                                "message", "Profile updated successfully",
                                                "id", user.getId(),
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "role", user.getRole(),
                                                "active", user.isActive()));
        }

        @GetMapping("/email/{email}")
        public ResponseEntity<?> getByEmail(@PathVariable String email) {

                User user = userService.findByEmail(email);

                return ResponseEntity.ok(
                                Map.of(
                                                "message", "Account retrieved successfully",
                                                "id", user.getId(),
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "role", user.getRole(),
                                                "active", user.isActive()));
        }
}