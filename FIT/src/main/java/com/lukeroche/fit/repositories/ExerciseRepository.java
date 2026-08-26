package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends CrudRepository<ExerciseEntity, Long>,
        PagingAndSortingRepository<ExerciseEntity, Long> {

    @Query("""
            SELECT DISTINCT e
            FROM ExerciseEntity e
            WHERE e.createdByUserId = :userId
              AND (e.tracksWeight IS NULL OR e.tracksWeight = TRUE)
              AND EXISTS (
                SELECT 1 FROM LoggedSetEntity ls
                JOIN ls.loggedExerciseEntity le
                JOIN le.workoutLogEntity wl
                WHERE le.exerciseEntity = e
                  AND wl.createdByUserId = :userId
                  AND wl.completedAt IS NOT NULL
                  AND (le.tracksWeight IS NULL OR le.tracksWeight = TRUE)
                  AND (
                    (ls.actualWeight IS NOT NULL AND ls.actualWeight > 0
                      AND ls.actualReps IS NOT NULL AND ls.actualReps > 0
                      AND (ls.failed IS NULL OR ls.failed = FALSE))
                    OR
                    (ls.rightWeight IS NOT NULL AND ls.rightWeight > 0
                      AND ls.rightReps IS NOT NULL AND ls.rightReps > 0
                      AND (ls.rightFailed IS NULL OR ls.rightFailed = FALSE))
                  )
              )
            ORDER BY e.name ASC
            """)
    List<ExerciseEntity> findTrainedWeightedExercises(@Param("userId") UUID userId);

    Page<ExerciseEntity> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Optional<ExerciseEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    Optional<ExerciseEntity> findFirstByCreatedByUserIdAndNameIgnoreCase(UUID createdByUserId, String name);

    List<ExerciseEntity> findByLoadingTypeIsNull();
}
