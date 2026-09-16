/*
 * Filename: UserService.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.user.MeResponse;
import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Accounts, self profile, and friend-visible profiles. Search excludes the
 * caller. Strangers looking up a profile get the same not-found as a missing
 * user.
 */
public interface UserService {
    User getUserById(UUID id);

    /** Batch load for the friends feed so each item is not a separate query. */
    Map<UUID, User> findByIds(Collection<UUID> ids);

    User createUser(String name, String username, String email, String rawPassword);
    Page<User> searchUsers(String query, UUID excludingUserId, Pageable pageable);
    MeResponse getMe(UUID userId);
    MeResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * Self or a friend. Anyone else gets not-found so friendship is not leaked.
     */
    UserProfileResponse getVisibleProfile(UUID viewerId, UUID targetId);
}
