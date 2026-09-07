package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WorkoutExerciseRepository extends CrudRepository<WorkoutExerciseEntity, Long>,
        PagingAndSortingRepository<WorkoutExerciseEntity, Long> {

    boolean existsByIdAndWorkoutEntity_Id(Long id, Long workoutId);

    boolean existsByExerciseEntity_Id(Long exerciseId);

    long countByWorkoutEntity_Id(Long workoutId);

    List<WorkoutExerciseEntity> findByWorkoutEntity_IdOrderByOrderIndexAsc(Long workoutId);
}
