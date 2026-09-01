package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.friend.FriendResponse;
import com.lukeroche.fit.domain.dto.friend.RelationshipStatus;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Friend requests and accepted friendships. A pair of users has at most one
 * row, stored in the direction of who sent the request.
 */
public interface FriendshipService {

    FriendshipEntity sendRequest(UUID requesterId, String recipientUsername);

    /** True only for a pending request addressed to this user. */
    boolean isRecipient(Long requestId, UUID userId);

    /** True if the user is requester or recipient, pending or accepted. */
    boolean isParty(Long requestId, UUID userId);

    FriendshipEntity acceptRequest(Long requestId);

    void removeRequest(Long requestId);

    void unfriend(UUID userId, UUID friendUserId);

    Page<FriendshipEntity> listIncoming(UUID userId, Pageable pageable);

    Page<FriendshipEntity> listOutgoing(UUID userId, Pageable pageable);

    /** Accepted friends, whether this user sent or received the request. */
    List<UUID> listFriendUserIds(UUID userId);

    Page<FriendResponse> listFriends(UUID userId, Pageable pageable);

    boolean isFriend(UUID userIdA, UUID userIdB);

    Optional<LocalDateTime> friendsSince(UUID userIdA, UUID userIdB);

    /**
     * {@code NONE}, {@code FRIENDS}, or pending in/out from this user's point
     * of view. Used by search so the UI can show Requested vs Add.
     */
    RelationshipStatus relationshipStatus(UUID userId, UUID otherUserId);
}
