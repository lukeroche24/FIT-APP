package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutRepository extends CrudRepository<WorkoutEntity, Long>,
        PagingAndSortingRepository<WorkoutEntity, Long> {
}
