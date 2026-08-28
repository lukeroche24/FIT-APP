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

public interface WorkoutLogService {

    WorkoutLogEntity startSession(Long sourceWorkoutId, UUID userId, StartSessionRequest request);

    Page<WorkoutLogEntity> findAllForUser(UUID userId, String query, Pageable pageable);

    Optional<WorkoutLogEntity> findInProgressForUser(UUID userId);

    Optional<WorkoutLogEntity> findOneForUser(Long id, UUID userId);

    Optional<WorkoutLogEntity> findVisibleToUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    WorkoutLogEntity partialUpdate(Long id, UUID userId, WorkoutLogEntity workoutLogEntity);

    WorkoutLogEntity finishSession(Long id, UUID userId);

    void delete(Long id);

    LoggedExerciseEntity addLoggedExercise(Long workoutLogId, UUID userId, AddLoggedExerciseRequest request);

    boolean loggedExerciseBelongsToWorkoutLog(Long loggedExerciseId, Long workoutLogId);

    void removeLoggedExercise(Long workoutLogId, Long loggedExerciseId);

    LoggedSetEntity addLoggedSet(Long loggedExerciseId, LoggedSetRequest request);

    LoggedSetEntity updateLoggedSet(Long setId, LoggedSetRequest request);

    void removeLoggedSet(Long loggedExerciseId, Long setId);

    boolean loggedSetBelongsToLoggedExercise(Long setId, Long loggedExerciseId);

    Page<WorkoutLogEntity> getFriendsFeed(UUID userId, Pageable pageable);

    Map<Long, List<String>> exerciseNamesByLogId(Collection<Long> logIds);

    boolean canCopy(Long workoutLogId, UUID userId);
}
