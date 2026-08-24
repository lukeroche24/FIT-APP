package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.repositories.UserRepository;
import com.lukeroche.fit.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public User createUser(String name, String username, String email, String rawPassword) {
        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Username must be 3-20 characters (letters, numbers, underscore)");
        }
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new IllegalStateException("Username already taken: " + username);
        });
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new IllegalStateException("Email already in use: " + email);
        });

        User user = User.builder()
                .name(name)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        return userRepository.save(user);
    }

    @Override
    public Page<User> searchUsers(String query, UUID excludingUserId, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseAndIdNot(query, excludingUserId, pageable);
    }

}
