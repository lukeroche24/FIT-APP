package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.repositories.UserRepository;
import com.lukeroche.fit.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(String name, String email, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new IllegalStateException("Email already in use: " + email);
        });

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        return userRepository.save(user);
    }

}
