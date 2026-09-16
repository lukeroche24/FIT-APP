/*
 * Filename: UserServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.user.UpdateProfileRequest;
import com.lukeroche.fit.domain.dto.user.UserProfileResponse;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTests extends AbstractServiceTests {

    @Autowired
    FriendshipService friendshipService;

    @Test
    void createUserRejectsDuplicateUsernameAndEmail() {
        User existing = newUser("ow");

        IllegalStateException usernameTaken = assertThrows(
                IllegalStateException.class,
                () -> userService.createUser("Other", existing.getUsername(), "other@ex.com", "password"));
        assertEquals("Username already taken: " + existing.getUsername(), usernameTaken.getMessage());

        IllegalStateException emailTaken = assertThrows(
                IllegalStateException.class,
                () -> userService.createUser("Other", "other_user", existing.getEmail(), "password"));
        assertEquals("Email already in use: " + existing.getEmail(), emailTaken.getMessage());
    }

    @Test
    void strangerProfileLooksLikeAMissingUserUntilTheyAreFriends() {
        User owner = newUser("ow");
        User friend = newUser("fr");
        User stranger = newUser("st");

        UserProfileResponse self = userService.getVisibleProfile(owner.getId(), owner.getId());
        assertEquals(owner.getUsername(), self.getUsername());

        EntityNotFoundException hidden = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getVisibleProfile(stranger.getId(), owner.getId()));
        assertEquals("User not found", hidden.getMessage());

        FriendshipEntity request = friendshipService.sendRequest(friend.getId(), owner.getUsername());
        friendshipService.acceptRequest(request.getId());

        UserProfileResponse visible = userService.getVisibleProfile(friend.getId(), owner.getId());
        assertEquals(owner.getId(), visible.getId());
        assertEquals(owner.getUsername(), visible.getUsername());
    }

    @Test
    void updateProfileCannotTakeAnotherUsersUsernameOrEmail() {
        User owner = newUser("ow");
        User other = newUser("ot");

        IllegalStateException usernameTaken = assertThrows(
                IllegalStateException.class,
                () -> userService.updateProfile(owner.getId(), UpdateProfileRequest.builder()
                        .name("Test")
                        .username(other.getUsername())
                        .email(owner.getEmail())
                        .build()));
        assertEquals("Username already taken: " + other.getUsername(), usernameTaken.getMessage());

        IllegalStateException emailTaken = assertThrows(
                IllegalStateException.class,
                () -> userService.updateProfile(owner.getId(), UpdateProfileRequest.builder()
                        .name("Test")
                        .username(owner.getUsername())
                        .email(other.getEmail())
                        .build()));
        assertEquals("Email already in use: " + other.getEmail(), emailTaken.getMessage());
    }
}
