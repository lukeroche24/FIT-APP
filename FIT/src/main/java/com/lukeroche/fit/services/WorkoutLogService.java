package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workoutlog.AddLoggedExerciseRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.StartSessionRequest;
import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Logged sessions: start from a workout template, record sets, finish, and
 * expose history to the owner or (when completed) to friends.
 */
public interface WorkoutLogService {

    /**
     * Copies the workout's exercises and planned sets into a new in-progress
     * log. Prefills reps/weight from {@link com.lukeroche.fit.services.progression.ProgressionService}
     * when history exists; otherwise uses the planned set. Throws if the user
     * already has an unfinished session.
     */
    WorkoutLogEntity startSession(Long sourceWorkoutId, UUID userId, StartSessionRequest request);

    Page<WorkoutLogEntity> findAllForUser(UUID userId, String query, Pageable pageable);

    Optional<WorkoutLogEntity> findInProgressForUser(UUID userId);

    Optional<WorkoutLogEntity> findOneForUser(Long id, UUID userId);

    /**
     * Owner always sees the log. Friends see it only after {@code completedAt}
     * is set (in-progress sessions stay private).
     */
    Optional<WorkoutLogEntity> findVisibleToUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    WorkoutLogEntity partialUpdate(Long id, UUID userId, WorkoutLogEntity workoutLogEntity);

    /** Sets {@code completedAt} so the session enters history and the feed. */
    WorkoutLogEntity finishSession(Long id, UUID userId);

    void delete(Long id);

    LoggedExerciseEntity addLoggedExercise(Long workoutLogId, UUID userId, AddLoggedExerciseRequest request);

    boolean loggedExerciseBelongsToWorkoutLog(Long loggedExerciseId, Long workoutLogId);

    void removeLoggedExercise(Long workoutLogId, Long loggedExerciseId);

    /**
     * Appends a set. Missing reps or weight are filled from the progression
     * suggestion, then snapped to the exercise load step.
     */
    LoggedSetEntity addLoggedSet(Long loggedExerciseId, LoggedSetRequest request);

    /**
     * Updates actuals, laterality, notes, and failed flags only. Target
     * reps/weight stay as seeded so a later hit/miss still compares against
     * what was asked, not what was typed.
     */
    LoggedSetEntity updateLoggedSet(Long setId, LoggedSetRequest request);

    void removeLoggedSet(Long loggedExerciseId, Long setId);

    boolean loggedSetBelongsToLoggedExercise(Long setId, Long loggedExerciseId);

    Page<WorkoutLogEntity> getFriendsFeed(UUID userId, Pageable pageable);

    Map<Long, List<String>> exerciseNamesByLogId(Collection<Long> logIds);

    boolean canCopy(Long workoutLogId, UUID userId);
}
