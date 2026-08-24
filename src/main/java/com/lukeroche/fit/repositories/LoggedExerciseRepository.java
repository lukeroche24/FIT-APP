package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoggedExerciseRepository extends CrudRepository<LoggedExerciseEntity, Long>,
        PagingAndSortingRepository<LoggedExerciseEntity, Long> {

    boolean existsByIdAndWorkoutLogEntity_Id(Long id, Long workoutLogId);

    long countByWorkoutLogEntity_Id(Long workoutLogId);

    List<LoggedExerciseEntity> findByWorkoutLogEntity_IdOrderByOrderIndexAsc(Long workoutLogId);
}
