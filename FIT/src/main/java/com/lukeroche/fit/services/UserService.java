package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.user.MeResponse;
import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
    User createUser(String name, String username, String email, String rawPassword);
    Page<User> searchUsers(String query, UUID excludingUserId, Pageable pageable);
    MeResponse getMe(UUID userId);
    MeResponse updateProfile(UUID userId, UpdateProfileRequest request);
    UserProfileResponse getVisibleProfile(UUID viewerId, UUID targetId);
}
