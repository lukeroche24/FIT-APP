package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.user.MeResponse;
import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.PlanEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.repositories.PlanRepository;
import com.lukeroche.fit.repositories.UserRepository;
import com.lukeroche.fit.repositories.WorkoutLogRepository;
import com.lukeroche.fit.repositories.WorkoutRepository;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistence for {@link UserService}. Profile stats are counts of owned
 * workouts and finished sessions plus the active plan name.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutRepository workoutRepository;
    private final PlanRepository planRepository;
    private final FriendshipService friendshipService;

    @Override
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public Map<UUID, User> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        Map<UUID, User> byId = new HashMap<>();
        for (User user : userRepository.findAllById(ids)) {
            byId.put(user.getId(), user);
        }
        return byId;
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

    @Override
    public MeResponse getMe(UUID userId) {
        return toMe(getUserById(userId));
    }

    @Override
    @Transactional
    public MeResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        String name = request.getName() == null ? "" : request.getName().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String password = request.getPassword();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("Username must be 3-20 characters (letters, numbers, underscore)");
        }
        if (email.isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required");
        }

        userRepository.findByUsername(username).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalStateException("Username already taken: " + username);
            }
        });
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalStateException("Email already in use: " + email);
            }
        });

        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return toMe(userRepository.save(user));
    }

    @Override
    public UserProfileResponse getVisibleProfile(UUID viewerId, UUID targetId) {
        if (!viewerId.equals(targetId) && !friendshipService.isFriend(viewerId, targetId)) {
            // Same not-found as a missing user so friendship is not leaked.
            throw new EntityNotFoundException("User not found");
        }

        User user = getUserById(targetId);
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .completedSessionCount(workoutLogRepository.countByCreatedByUserIdAndCompletedAtIsNotNull(targetId))
                .workoutCount(workoutRepository.countByCreatedByUserId(targetId))
                .activePlanName(activePlanName(targetId))
                .friendsSince(friendshipService.friendsSince(viewerId, targetId).orElse(null))
                .build();
    }

    private MeResponse toMe(User user) {
        return MeResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .completedSessionCount(workoutLogRepository.countByCreatedByUserIdAndCompletedAtIsNotNull(user.getId()))
                .workoutCount(workoutRepository.countByCreatedByUserId(user.getId()))
                .activePlanName(activePlanName(user.getId()))
                .build();
    }

    private String activePlanName(UUID userId) {
        return planRepository.findByCreatedByUserIdAndActiveTrue(userId)
                .map(PlanEntity::getName)
                .orElse(null);
    }

}
