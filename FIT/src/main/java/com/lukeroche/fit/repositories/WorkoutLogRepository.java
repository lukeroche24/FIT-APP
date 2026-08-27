package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
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

    Optional<WorkoutLogEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    Optional<WorkoutLogEntity> findFirstByCreatedByUserIdAndCompletedAtIsNullOrderByStartedAtDesc(
            UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    List<WorkoutLogEntity> findByCreatedByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            UUID createdByUserId, LocalDateTime from, LocalDateTime to);

    Page<WorkoutLogEntity> findByCreatedByUserIdInAndCompletedAtIsNotNullOrderByCompletedAtDesc(
            Collection<UUID> createdByUserIds, Pageable pageable);

    long countByCreatedByUserIdAndCompletedAtIsNotNull(UUID createdByUserId);
}
