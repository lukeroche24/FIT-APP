package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlannedSetRepository extends CrudRepository<PlannedSetEntity, Long>,
        PagingAndSortingRepository<PlannedSetEntity, Long> {

    boolean existsByIdAndWorkoutExerciseEntity_Id(Long id, Long workoutExerciseId);

    long countByWorkoutExerciseEntity_Id(Long workoutExerciseId);

    List<PlannedSetEntity> findByWorkoutExerciseEntity_IdOrderBySetNumberAsc(Long workoutExerciseId);
}
