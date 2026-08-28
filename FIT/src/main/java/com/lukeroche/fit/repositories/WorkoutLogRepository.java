package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutLogRepository extends CrudRepository<WorkoutLogEntity, Long>,
        PagingAndSortingRepository<WorkoutLogEntity, Long> {

    Page<WorkoutLogEntity> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Page<WorkoutLogEntity> findByCreatedByUserIdAndNameContainingIgnoreCase(
            UUID createdByUserId, String name, Pageable pageable);

    Optional<WorkoutLogEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    Optional<WorkoutLogEntity> findFirstByCreatedByUserIdAndCompletedAtIsNullOrderByStartedAtDesc(
            UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    List<WorkoutLogEntity> findByCreatedByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            UUID createdByUserId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT wl FROM WorkoutLogEntity wl
            WHERE wl.completedAt IS NOT NULL
              AND wl.createdByUserId IN :friendIds
              AND wl.completedAt >= (
                  SELECT COALESCE(f.acceptedAt, f.createdAt)
                  FROM FriendshipEntity f
                  WHERE f.status = com.lukeroche.fit.domain.entities.FriendshipStatus.ACCEPTED
                    AND (
                        (f.requesterId = :viewerId AND f.recipientId = wl.createdByUserId)
                        OR (f.recipientId = :viewerId AND f.requesterId = wl.createdByUserId)
                    )
              )
            """)
    Page<WorkoutLogEntity> findFriendsFeed(
            @Param("viewerId") UUID viewerId,
            @Param("friendIds") Collection<UUID> friendIds,
            Pageable pageable);

    long countByCreatedByUserIdAndCompletedAtIsNotNull(UUID createdByUserId);
}
