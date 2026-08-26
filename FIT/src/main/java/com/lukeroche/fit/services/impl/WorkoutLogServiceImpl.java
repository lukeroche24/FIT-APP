package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.workoutlog.AddLoggedExerciseRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.StartSessionRequest;
import com.lukeroche.fit.domain.entities.*;
import com.lukeroche.fit.repositories.*;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.WorkoutLogService;
import com.lukeroche.fit.services.progression.ProgressionService;
import com.lukeroche.fit.services.progression.Recommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkoutLogServiceImpl implements WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;
    private final LoggedExerciseRepository loggedExerciseRepository;
    private final LoggedSetRepository loggedSetRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final PlannedSetRepository plannedSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final FriendshipService friendshipService;
    private final ProgressionService progressionService;

    public WorkoutLogServiceImpl(WorkoutLogRepository workoutLogRepository,
                                  LoggedExerciseRepository loggedExerciseRepository,
                                  LoggedSetRepository loggedSetRepository,
                                  WorkoutRepository workoutRepository,
                                  WorkoutExerciseRepository workoutExerciseRepository,
                                  PlannedSetRepository plannedSetRepository,
                                  ExerciseRepository exerciseRepository,
                                  FriendshipService friendshipService,
                                  ProgressionService progressionService) {
        this.workoutLogRepository = workoutLogRepository;
        this.loggedExerciseRepository = loggedExerciseRepository;
        this.loggedSetRepository = loggedSetRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.plannedSetRepository = plannedSetRepository;
        this.exerciseRepository = exerciseRepository;
        this.friendshipService = friendshipService;
        this.progressionService = progressionService;
    }

    @Override
    @Transactional
    public WorkoutLogEntity startSession(Long sourceWorkoutId, UUID userId, StartSessionRequest request) {
        WorkoutEntity sourceWorkout = workoutRepository
                .findByIdAndCreatedByUserId(sourceWorkoutId, userId)
                .orElseThrow(() -> new RuntimeException("Workout does not exist"));

        WorkoutLogEntity workoutLog = workoutLogRepository.save(WorkoutLogEntity.builder()
                .createdByUserId(userId)
                .sourceWorkoutId(sourceWorkoutId)
                .name(request != null && request.getName() != null ? request.getName() : sourceWorkout.getName())
                .startedAt(LocalDateTime.now())
                .completedAt(null)
                .build());

        List<WorkoutExerciseEntity> sourceExercises =
                workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(sourceWorkoutId);

        for (WorkoutExerciseEntity workoutExercise : sourceExercises) {
            LoggedExerciseEntity loggedExercise = loggedExerciseRepository.save(LoggedExerciseEntity.builder()
                    .workoutLogEntity(workoutLog)
                    .exerciseEntity(workoutExercise.getExerciseEntity())
                    .orderIndex(workoutExercise.getOrderIndex())
                    .build());

            List<PlannedSetEntity> plannedSets =
                    plannedSetRepository.findByWorkoutExerciseEntity_IdOrderBySetNumberAsc(workoutExercise.getId());

            Recommendation suggestion = progressionService.forExercise(
                    userId,
                    workoutExercise.getExerciseEntity(),
                    workoutExercise.getMinReps(),
                    workoutExercise.getMaxReps());

            List<LoggedSetEntity> loggedSets = plannedSets.stream()
                    .map(plannedSet -> {
                        Integer reps = suggestion.targetReps() != null
                                ? suggestion.targetReps()
                                : plannedSet.getTargetReps();
                        Float weight = suggestion.targetWeight() != null
                                ? Float.valueOf(suggestion.targetWeight().floatValue())
                                : plannedSet.getTargetWeight();
                        return LoggedSetEntity.builder()
                                .loggedExerciseEntity(loggedExercise)
                                .setNumber(plannedSet.getSetNumber())
                                .actualReps(reps)
                                .actualWeight(weight)
                                .targetReps(reps)
                                .targetWeight(weight)
                                .loggedAt(null)
                                .build();
                    })
                    .toList();
            loggedSetRepository.saveAll(loggedSets);
            loggedExercise.getLoggedSets().addAll(loggedSets);
            workoutLog.getLoggedExercises().add(loggedExercise);
        }

        return workoutLog;
    }

    @Override
    public Page<WorkoutLogEntity> findAllForUser(UUID userId, Pageable pageable) {
        return workoutLogRepository.findByCreatedByUserId(userId, pageable);
    }

    @Override
    public Optional<WorkoutLogEntity> findOneForUser(Long id, UUID userId) {
        return workoutLogRepository.findByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public boolean isOwnedByUser(Long id, UUID userId) {
        return workoutLogRepository.existsByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public WorkoutLogEntity partialUpdate(Long id, UUID userId, WorkoutLogEntity workoutLogEntity) {
        return workoutLogRepository.findByIdAndCreatedByUserId(id, userId).map(existingLog -> {
            Optional.ofNullable(workoutLogEntity.getName()).ifPresent(existingLog::setName);
            Optional.ofNullable(workoutLogEntity.getNotes()).ifPresent(existingLog::setNotes);
            return workoutLogRepository.save(existingLog);
        }).orElseThrow(() -> new RuntimeException("Workout log does not exist"));
    }

    @Override
    public WorkoutLogEntity finishSession(Long id, UUID userId) {
        return workoutLogRepository.findByIdAndCreatedByUserId(id, userId).map(existingLog -> {
            existingLog.setCompletedAt(LocalDateTime.now());
            return workoutLogRepository.save(existingLog);
        }).orElseThrow(() -> new RuntimeException("Workout log does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutLogRepository.deleteById(id);
    }

    @Override
    public LoggedExerciseEntity addLoggedExercise(Long workoutLogId, UUID userId, AddLoggedExerciseRequest request) {
        WorkoutLogEntity workoutLog = workoutLogRepository.findByIdAndCreatedByUserId(workoutLogId, userId).orElseThrow();

        ExerciseEntity exercise = exerciseRepository.findByIdAndCreatedByUserId(request.getExerciseId(), userId).orElseThrow();

        LoggedExerciseEntity loggedExercise = LoggedExerciseEntity.builder()
                .workoutLogEntity(workoutLog)
                .exerciseEntity(exercise)
                .orderIndex(loggedExerciseRepository.countByWorkoutLogEntity_Id(workoutLogId) + 1)
                .build();

        return loggedExerciseRepository.save(loggedExercise);
    }

    @Override
    public boolean loggedExerciseBelongsToWorkoutLog(Long loggedExerciseId, Long workoutLogId) {
        return loggedExerciseRepository.existsByIdAndWorkoutLogEntity_Id(loggedExerciseId, workoutLogId);
    }

    @Override
    @Transactional
    public void removeLoggedExercise(Long workoutLogId, Long loggedExerciseId) {
        loggedExerciseRepository.deleteById(loggedExerciseId);

        List<LoggedExerciseEntity> loggedExercises =
                loggedExerciseRepository.findByWorkoutLogEntity_IdOrderByOrderIndexAsc(workoutLogId);

        for (int i = 0; i < loggedExercises.size(); i++) {
            loggedExercises.get(i).setOrderIndex((long) i + 1);
        }
        loggedExerciseRepository.saveAll(loggedExercises);
    }

    @Override
    @Transactional
    public LoggedSetEntity addLoggedSet(Long loggedExerciseId, LoggedSetRequest request) {
        LoggedExerciseEntity loggedExercise = loggedExerciseRepository.findById(loggedExerciseId).orElseThrow();

        Integer reps = request != null ? request.getActualReps() : null;
        Float weight = request != null ? request.getActualWeight() : null;
        String notes = request != null ? request.getNotes() : null;

        if (reps == null || weight == null) {
            Recommendation suggestion = progressionService.forExercise(
                    loggedExercise.getWorkoutLogEntity().getCreatedByUserId(),
                    loggedExercise.getExerciseEntity());
            if (reps == null) {
                reps = suggestion.targetReps();
            }
            if (weight == null && suggestion.targetWeight() != null) {
                weight = suggestion.targetWeight().floatValue();
            }
        }

        LoggedSetEntity loggedSet = LoggedSetEntity.builder()
                .loggedExerciseEntity(loggedExercise)
                .setNumber((int) (loggedSetRepository.countByLoggedExerciseEntity_Id(loggedExerciseId) + 1))
                .actualReps(reps)
                .actualWeight(weight)
                .targetReps(reps)
                .targetWeight(weight)
                //.actualDurationSeconds(request.getActualDurationSeconds())
                //.actualDistance(request.getActualDistance())
                .notes(notes)
                .loggedAt(null)
                .build();

        return loggedSetRepository.save(loggedSet);
    }

    @Override
    public LoggedSetEntity updateLoggedSet(Long setId, LoggedSetRequest request) {
        return loggedSetRepository.findById(setId).map(existingSet -> {
            Optional.ofNullable(request.getActualReps()).ifPresent(existingSet::setActualReps);
            Optional.ofNullable(request.getActualWeight()).ifPresent(existingSet::setActualWeight);
            //Optional.ofNullable(request.getActualDurationSeconds()).ifPresent(existingSet::setActualDurationSeconds);
            //Optional.ofNullable(request.getActualDistance()).ifPresent(existingSet::setActualDistance);
            Optional.ofNullable(request.getNotes()).ifPresent(existingSet::setNotes);
            existingSet.setLoggedAt(LocalDateTime.now());
            return loggedSetRepository.save(existingSet);
        }).orElseThrow(() -> new RuntimeException("Logged set does not exist"));
    }

    @Override
    @Transactional
    public void removeLoggedSet(Long loggedExerciseId, Long setId) {
        loggedSetRepository.deleteById(setId);

        List<LoggedSetEntity> remaining =
                loggedSetRepository.findByLoggedExerciseEntity_IdOrderBySetNumberAsc(loggedExerciseId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSetNumber(i + 1);
        }
        loggedSetRepository.saveAll(remaining);
    }

    @Override
    public boolean loggedSetBelongsToLoggedExercise(Long setId, Long loggedExerciseId) {
        return loggedSetRepository.existsByIdAndLoggedExerciseEntity_Id(setId, loggedExerciseId);
    }

    @Override
    public Page<WorkoutLogEntity> getFriendsFeed(UUID userId, Pageable pageable) {
        List<UUID> friendIds = friendshipService.listFriendUserIds(userId);
        if (friendIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return workoutLogRepository.findByCreatedByUserIdInAndCompletedAtIsNotNullOrderByCompletedAtDesc(friendIds, pageable);
    }

    @Override
    public boolean canCopy(Long workoutLogId, UUID userId) {
        Optional<WorkoutLogEntity> found = workoutLogRepository.findById(workoutLogId);
        if (found.isEmpty()) {
            return false;
        }
        WorkoutLogEntity log = found.get();
        if (log.getCompletedAt() == null) {
            return false;
        }
        if (log.getCreatedByUserId().equals(userId)) {
            return true;
        }
        return friendshipService.isFriend(userId, log.getCreatedByUserId());
    }
}
