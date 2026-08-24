package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends CrudRepository<FriendshipEntity, Long>,
        PagingAndSortingRepository<FriendshipEntity, Long> {

    Optional<FriendshipEntity> findByRequesterIdAndRecipientId(UUID requesterId, UUID recipientId);

    Page<FriendshipEntity> findByRecipientIdAndStatus(UUID recipientId, FriendshipStatus status, Pageable pageable);

    Page<FriendshipEntity> findByRequesterIdAndStatus(UUID requesterId, FriendshipStatus status, Pageable pageable);

    List<FriendshipEntity> findByRequesterIdAndStatus(UUID requesterId, FriendshipStatus status);

    List<FriendshipEntity> findByRecipientIdAndStatus(UUID recipientId, FriendshipStatus status);

    boolean existsByRequesterIdAndRecipientIdAndStatus(UUID requesterId, UUID recipientId, FriendshipStatus status);
}
