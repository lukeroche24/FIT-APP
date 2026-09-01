package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workout.UpdateWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Workout templates the caller owns. Nested exercises and planned sets are
 * the prescription; starting a session clones them into a log.
 */
public interface WorkoutService {
    WorkoutEntity save(WorkoutEntity workout);

    Page<WorkoutEntity> findAllForUser(UUID userId, String query, Pageable pageable);

    Optional<WorkoutEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    WorkoutEntity partialUpdate(Long id, UUID userId, WorkoutEntity workoutEntity);

    void delete(Long id);

    /**
     * Appends an owned exercise. Missing rep range defaults to 6–12 (swapped
     * if inverted). Tracking and laterality inherit from the exercise unless
     * the request overrides them.
     */
    WorkoutExerciseEntity addWorkoutExercise(Long workoutId, UUID userId, AddWorkoutExerciseRequest request);

    /**
     * Patches tracking, laterality, and rep range. A non-null {@code orderIndex}
     * is 1-based and rewrites the order of every exercise in the workout.
     */
    WorkoutExerciseEntity updateWorkoutExercise(Long workoutId, Long workoutExerciseId, UpdateWorkoutExerciseRequest request);

    boolean workoutExerciseBelongsToWorkout(Long workoutExerciseId, Long workoutId);

    void removeExerciseFromWorkout(Long workoutId, Long workoutExerciseId);

    PlannedSetEntity addPlannedSet(Long workoutExerciseId, PlannedSetRequest request);

    PlannedSetEntity updatePlannedSet(Long setId, PlannedSetRequest request);

    void removePlannedSet(Long workoutExerciseId, Long setId);

    boolean plannedSetBelongsToWorkoutExercise(Long setId, Long workoutExerciseId);

    /**
     * New workout and planned-set rows with no FK back to the source log.
     * Exercises are reused from the copier's library when the name matches,
     * otherwise a new library row is created. Rep range comes from the source
     * workout when known, otherwise from logged sets, else 6–12.
     */
    WorkoutEntity copyWorkoutLogToLibrary(Long workoutLogId, UUID copyingUserId);

}
