package com.ridelink.account.service;

import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.entity.User;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        public UserService(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        public User register(RegisterRequest request) {
                String email = normalizeEmail(request.getEmail());

                String role = request.getRole() == null
                                ? ""
                                : request.getRole().trim().toUpperCase(Locale.ROOT);

                if (!role.equals("RIDER") && !role.equals("DRIVER")) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Role must be RIDER or DRIVER");
                }

                if (userRepository.existsByEmail(email)) {
                        throw new ApiException(
                                        HttpStatus.CONFLICT,
                                        "Email already registered");
                }

                User user = new User(
                                request.getFullName().trim(),
                                email,
                                passwordEncoder.encode(request.getPassword()),
                                role);

                return userRepository.save(user);
        }

        public User findByEmail(String email) {
                return userRepository.findByEmail(normalizeEmail(email))
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found"));
        }

        public User authenticate(String email, String password) {
                User user = userRepository.findByEmail(normalizeEmail(email))
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid email or password"));

                if (!passwordEncoder.matches(password, user.getPassword())) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid email or password");
                }

                if (!user.isActive()) {
                        throw new ApiException(
                                        HttpStatus.FORBIDDEN,
                                        "Account is inactive");
                }

                return user;
        }

        public User findActiveById(String id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Account no longer exists"));

                if (!user.isActive()) {
                        throw new ApiException(
                                        HttpStatus.FORBIDDEN,
                                        "Account is inactive");
                }

                return user;
        }

        public User updateProfile(String userId, String fullName) {
                User user = findActiveById(userId);

                user.setFullName(fullName.trim());

                return userRepository.save(user);
        }

        private String normalizeEmail(String email) {
                return email.trim().toLowerCase(Locale.ROOT);
        }
}