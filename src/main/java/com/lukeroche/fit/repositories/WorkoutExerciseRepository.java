package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WorkoutExerciseRepository extends CrudRepository<WorkoutExerciseEntity, Long>,
        PagingAndSortingRepository<WorkoutExerciseEntity, Long> {
}
