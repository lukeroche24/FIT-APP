package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoggedSetRepository extends CrudRepository<LoggedSetEntity, Long>,
        PagingAndSortingRepository<LoggedSetEntity, Long> {

    boolean existsByIdAndLoggedExerciseEntity_Id(Long id, Long loggedExerciseId);

    long countByLoggedExerciseEntity_Id(Long loggedExerciseId);

    List<LoggedSetEntity> findByLoggedExerciseEntity_IdOrderBySetNumberAsc(Long loggedExerciseId);

    @Query(""" 
            SELECT NEW com.lukeroche.fit.domain.projections.SetHistoryRow(
                wl.id,
                wl.completedAt,
                ls.setNumber,
                ls.actualReps,
                ls.actualWeight,
                ls.rightReps,
                ls.rightWeight,
                ls.targetReps,
                ls.targetWeight,
                ls.failed,
                ls.rightFailed)
            FROM LoggedSetEntity ls
                join ls.loggedExerciseEntity le
                join le.workoutLogEntity wl
            WHERE wl.createdByUserId = :userId
                AND le.exerciseEntity.id = :exerciseId
                AND wl.completedAt IS NOT NULL
                AND ((ls.actualReps IS NOT NULL AND ls.actualReps > 0)
                    OR (ls.rightReps IS NOT NULL AND ls.rightReps > 0))
            ORDER BY wl.completedAt asc, ls.setNumber asc
""")
    List<SetHistoryRow> findCompletedSetHistory(@Param("userId") UUID userId,
                                                @Param("exerciseId") Long exerciseId);
}
