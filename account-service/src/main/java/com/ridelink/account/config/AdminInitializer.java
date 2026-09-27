package com.ridelink.account.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import com.ridelink.account.entity.User;
import com.ridelink.account.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.bootstrap-admin",
        name = "enabled",
        havingValue = "true"
)
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final String fullName;
    private final String email;
    private final String password;

    public AdminInitializer(
            UserRepository userRepository,
            @Value("${app.bootstrap-admin.full-name}") String fullName,
            @Value("${app.bootstrap-admin.email}") String email,
            @Value("${app.bootstrap-admin.password}") String password) {

        this.userRepository = userRepository;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (fullName.isBlank() || normalizedEmail.isBlank()) {
            throw new IllegalStateException(
                    "Admin setup requires a full name and email"
            );
        }

        if (password.isBlank()
                || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Admin password must contain at least 8 characters "
                            + "and must not exceed 72 UTF-8 bytes"
            );
        }

        Optional<User> existingUser =
                userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            if (!"ADMIN".equals(existingUser.get().getRole())) {
                throw new IllegalStateException(
                        "Admin setup email already belongs to a non-admin account. "
                                + "Choose a separate admin email."
                );
            }

            log.info("Admin account already exists; no changes made.");
            return;
        }

        BCryptPasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        User admin = new User(
                fullName.trim(),
                normalizedEmail,
                passwordEncoder.encode(password),
                "ADMIN"
        );

        userRepository.save(admin);

        log.info("Initial admin account created successfully.");
    }
}