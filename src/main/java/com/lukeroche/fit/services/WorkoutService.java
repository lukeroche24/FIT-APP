package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WorkoutService {
    WorkoutEntity save(WorkoutEntity workout);

    List<WorkoutEntity> findAll();

    Page<WorkoutEntity> findAll(Pageable pageable);

    Optional<WorkoutEntity> findOne(Long id);

    boolean isExists(Long id);

    WorkoutEntity partialUpdate(Long id, WorkoutEntity workoutEntity);

    void delete(Long id);
}
