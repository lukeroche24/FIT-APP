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

public interface FriendshipService {

    FriendshipEntity sendRequest(UUID requesterId, String recipientUsername);

    boolean isRecipient(Long requestId, UUID userId);

    boolean isParty(Long requestId, UUID userId);

    FriendshipEntity acceptRequest(Long requestId);

    void removeRequest(Long requestId);

    void unfriend(UUID userId, UUID friendUserId);

    Page<FriendshipEntity> listIncoming(UUID userId, Pageable pageable);

    Page<FriendshipEntity> listOutgoing(UUID userId, Pageable pageable);

    List<UUID> listFriendUserIds(UUID userId);

    Page<FriendResponse> listFriends(UUID userId, Pageable pageable);

    boolean isFriend(UUID userIdA, UUID userIdB);

    Optional<LocalDateTime> friendsSince(UUID userIdA, UUID userIdB);

    RelationshipStatus relationshipStatus(UUID userId, UUID otherUserId);
}
