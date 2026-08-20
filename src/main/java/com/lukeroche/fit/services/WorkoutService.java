package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.UpdateWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface WorkoutService {
    WorkoutEntity save(WorkoutEntity workout);

    Page<WorkoutEntity> findAllForUser(UUID userId, Pageable pageable);

    Optional<WorkoutEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    WorkoutEntity partialUpdate(Long id, UUID userId, WorkoutEntity workoutEntity);

    void delete(Long id);

    WorkoutExerciseEntity addWorkoutExercise(Long workoutId, UUID userId, AddWorkoutExerciseRequest request);

    WorkoutExerciseEntity reorderWorkoutExercise(Long workoutId, Long workoutExerciseID, UpdateWorkoutExerciseRequest request);

    boolean workoutExerciseBelongsToWorkout(Long workoutExerciseId, Long workoutId);

    void removeExerciseFromWorkout(Long workoutId, Long workoutExerciseId);

    PlannedSetEntity addPlannedSet(Long workoutExerciseId, PlannedSetRequest request);

    PlannedSetEntity updatePlannedSet(Long setId, PlannedSetRequest request);

    void removePlannedSet(Long workoutExerciseId, Long setId);

    boolean plannedSetBelongsToWorkoutExercise(Long setId, Long workoutExerciseId);

}
