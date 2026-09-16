/*
 * Filename: FriendshipServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.friend.RelationshipStatus;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FriendshipServiceTests extends AbstractServiceTests {

    @Autowired
    FriendshipService friendshipService;

    @Test
    void cannotRequestYourselfOrOpenASecondRelationship() {
        User owner = newUser("ow");
        User other = newUser("ot");

        IllegalArgumentException self = assertThrows(
                IllegalArgumentException.class,
                () -> friendshipService.sendRequest(owner.getId(), owner.getUsername()));
        assertEquals("Cannot send a friend request to yourself", self.getMessage());

        friendshipService.sendRequest(owner.getId(), other.getUsername());

        IllegalStateException duplicate = assertThrows(
                IllegalStateException.class,
                () -> friendshipService.sendRequest(other.getId(), owner.getUsername()));
        assertEquals("A friend request or friendship already exists with this user", duplicate.getMessage());
    }

    @Test
    void unknownUsernameLooksLikeAMissingUser() {
        User owner = newUser("ow");

        EntityNotFoundException missing = assertThrows(
                EntityNotFoundException.class,
                () -> friendshipService.sendRequest(owner.getId(), "nobody_here"));
        assertEquals("No user found with username: nobody_here", missing.getMessage());
    }

    @Test
    void relationshipStatusFollowsRequestAcceptAndUnfriend() {
        User owner = newUser("ow");
        User other = newUser("ot");

        assertEquals(RelationshipStatus.NONE, friendshipService.relationshipStatus(owner.getId(), other.getId()));

        FriendshipEntity request = friendshipService.sendRequest(owner.getId(), other.getUsername());
        assertEquals(RelationshipStatus.PENDING_OUTGOING,
                friendshipService.relationshipStatus(owner.getId(), other.getId()));
        assertEquals(RelationshipStatus.PENDING_INCOMING,
                friendshipService.relationshipStatus(other.getId(), owner.getId()));
        assertFalse(friendshipService.isFriend(owner.getId(), other.getId()));
        assertTrue(friendshipService.isRecipient(request.getId(), other.getId()));
        assertFalse(friendshipService.isRecipient(request.getId(), owner.getId()));

        friendshipService.acceptRequest(request.getId());
        assertEquals(RelationshipStatus.FRIENDS, friendshipService.relationshipStatus(owner.getId(), other.getId()));
        assertEquals(RelationshipStatus.FRIENDS, friendshipService.relationshipStatus(other.getId(), owner.getId()));
        assertTrue(friendshipService.isFriend(owner.getId(), other.getId()));
        assertFalse(friendshipService.isRecipient(request.getId(), other.getId()));

        friendshipService.unfriend(owner.getId(), other.getId());
        assertEquals(RelationshipStatus.NONE, friendshipService.relationshipStatus(owner.getId(), other.getId()));
        assertFalse(friendshipService.isFriend(owner.getId(), other.getId()));
    }
}
