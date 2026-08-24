package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutLogRepository extends CrudRepository<WorkoutLogEntity, Long>,
        PagingAndSortingRepository<WorkoutLogEntity, Long> {

    Page<WorkoutLogEntity> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Optional<WorkoutLogEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    Page<WorkoutLogEntity> findByCreatedByUserIdInAndCompletedAtIsNotNullOrderByCompletedAtDesc(
            Collection<UUID> createdByUserIds, Pageable pageable);
}
