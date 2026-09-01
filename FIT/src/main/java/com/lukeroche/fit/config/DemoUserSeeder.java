package com.lukeroche.fit.config;

import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Optional local login ({@code user@test.com} / {@code password}). Off unless
 * {@code fit.demo-user.enabled=true}. Not part of the security filter chain.
 */
@Component
@Slf4j
public class DemoUserSeeder implements ApplicationRunner {

    private static final String DEMO_EMAIL = "user@test.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DemoUserSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${fit.demo-user.enabled:false}") boolean enabled) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        userRepository.findByEmail(DEMO_EMAIL).orElseGet(() -> {
            log.info("Creating demo user {}", DEMO_EMAIL);
            return userRepository.save(User.builder()
                    .name("Test User")
                    .username("user_test")
                    .email(DEMO_EMAIL)
                    .password(passwordEncoder.encode("password"))
                    .build());
        });
    }
}
