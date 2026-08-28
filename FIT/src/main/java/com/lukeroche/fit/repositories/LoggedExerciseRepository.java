package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LoggedExerciseRepository extends CrudRepository<LoggedExerciseEntity, Long>,
        PagingAndSortingRepository<LoggedExerciseEntity, Long> {

    boolean existsByIdAndWorkoutLogEntity_Id(Long id, Long workoutLogId);

    boolean existsByExerciseEntity_Id(Long exerciseId);

    long countByWorkoutLogEntity_Id(Long workoutLogId);

    List<LoggedExerciseEntity> findByWorkoutLogEntity_IdOrderByOrderIndexAsc(Long workoutLogId);

    @Query("""
            SELECT le.workoutLogEntity.id AS workoutLogId, le.exerciseEntity.name AS name
            FROM LoggedExerciseEntity le
            WHERE le.workoutLogEntity.id IN :logIds
            ORDER BY le.orderIndex ASC
            """)
    List<LogExerciseName> findExerciseNamesForLogs(@Param("logIds") Collection<Long> logIds);

    interface LogExerciseName {
        Long getWorkoutLogId();
        String getName();
    }
}
