package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.friend.FriendResponse;
import com.lukeroche.fit.domain.dto.friend.RelationshipStatus;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.FriendshipStatus;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.repositories.FriendshipRepository;
import com.lukeroche.fit.repositories.UserRepository;
import com.lukeroche.fit.services.FriendshipService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for {@link FriendshipService}. Lookups try both
 * requester/recipient directions because the stored order is who sent first.
 */
@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    private Optional<FriendshipEntity> findRelationship(UUID userA, UUID userB) {
        return friendshipRepository.findByRequesterIdAndRecipientId(userA, userB)
                .or(() -> friendshipRepository.findByRequesterIdAndRecipientId(userB, userA));
    }

    @Override
    public FriendshipEntity sendRequest(UUID requesterId, String recipientUsername) {
        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new EntityNotFoundException("No user found with username: " + recipientUsername));

        if (recipient.getId().equals(requesterId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }

        findRelationship(requesterId, recipient.getId()).ifPresent(existing -> {
            throw new IllegalStateException("A friend request or friendship already exists with this user");
        });

        FriendshipEntity friendship = FriendshipEntity.builder()
                .requesterId(requesterId)
                .recipientId(recipient.getId())
                .status(FriendshipStatus.PENDING)
                .build();

        return friendshipRepository.save(friendship);
    }

    @Override
    public boolean isRecipient(Long requestId, UUID userId) {
        return friendshipRepository.findById(requestId)
                .filter(f -> f.getStatus() == FriendshipStatus.PENDING)
                .map(f -> f.getRecipientId().equals(userId))
                .orElse(false);
    }

    @Override
    public boolean isParty(Long requestId, UUID userId) {
        return friendshipRepository.findById(requestId)
                .map(f -> f.getRequesterId().equals(userId) || f.getRecipientId().equals(userId))
                .orElse(false);
    }

    @Override
    public FriendshipEntity acceptRequest(Long requestId) {
        FriendshipEntity friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Friend request does not exist"));
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setAcceptedAt(LocalDateTime.now());
        return friendshipRepository.save(friendship);
    }

    @Override
    public void removeRequest(Long requestId) {
        friendshipRepository.deleteById(requestId);
    }

    @Override
    public void unfriend(UUID userId, UUID friendUserId) {
        findRelationship(userId, friendUserId).ifPresent(friendshipRepository::delete);
    }

    @Override
    public Page<FriendshipEntity> listIncoming(UUID userId, Pageable pageable) {
        return friendshipRepository.findByRecipientIdAndStatus(userId, FriendshipStatus.PENDING, pageable);
    }

    @Override
    public Page<FriendshipEntity> listOutgoing(UUID userId, Pageable pageable) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING, pageable);
    }

    @Override
    public List<UUID> listFriendUserIds(UUID userId) {
        List<UUID> friendIds = new ArrayList<>();
        friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.ACCEPTED)
                .forEach(f -> friendIds.add(f.getRecipientId()));
        friendshipRepository.findByRecipientIdAndStatus(userId, FriendshipStatus.ACCEPTED)
                .forEach(f -> friendIds.add(f.getRequesterId()));
        return friendIds;
    }

    @Override
    public Page<FriendResponse> listFriends(UUID userId, Pageable pageable) {
        List<UUID> friendIds = listFriendUserIds(userId);

        // Friendship can be stored in either direction, so ids are gathered then paged in memory.
        List<FriendResponse> friends = friendIds.stream()
                .map(friendId -> {
                    User friend = userRepository.findById(friendId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    FriendshipEntity friendship = findRelationship(userId, friendId)
                            .orElseThrow(() -> new EntityNotFoundException("Friendship not found"));
                    return FriendResponse.builder()
                            .userId(friend.getId())
                            .username(friend.getUsername())
                            .name(friend.getName())
                            .friendsSince(friendsSinceOf(friendship))
                            .build();
                })
                .toList();

        int start = Math.min((int) pageable.getOffset(), friends.size());
        int end = Math.min(start + pageable.getPageSize(), friends.size());
        return new org.springframework.data.domain.PageImpl<>(friends.subList(start, end), pageable, friends.size());
    }

    @Override
    public boolean isFriend(UUID userIdA, UUID userIdB) {
        return findRelationship(userIdA, userIdB)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    @Override
    public Optional<LocalDateTime> friendsSince(UUID userIdA, UUID userIdB) {
        return findRelationship(userIdA, userIdB)
                .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .map(FriendshipServiceImpl::friendsSinceOf);
    }

    private static LocalDateTime friendsSinceOf(FriendshipEntity friendship) {
        // Older rows may lack acceptedAt; createdAt is the request time in that case.
        return friendship.getAcceptedAt() != null ? friendship.getAcceptedAt() : friendship.getCreatedAt();
    }

    @Override
    public RelationshipStatus relationshipStatus(UUID userId, UUID otherUserId) {
        Optional<FriendshipEntity> relationship = findRelationship(userId, otherUserId);
        if (relationship.isEmpty()) {
            return RelationshipStatus.NONE;
        }
        FriendshipEntity friendship = relationship.get();
        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            return RelationshipStatus.FRIENDS;
        }
        return friendship.getRequesterId().equals(userId)
                ? RelationshipStatus.PENDING_OUTGOING
                : RelationshipStatus.PENDING_INCOMING;
    }
}
