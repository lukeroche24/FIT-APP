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
import com.lukeroche.fit.services.progression.LoadingSchemeFactory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final LoadingSchemeFactory loadingSchemeFactory;

    public WorkoutLogServiceImpl(WorkoutLogRepository workoutLogRepository,
                                  LoggedExerciseRepository loggedExerciseRepository,
                                  LoggedSetRepository loggedSetRepository,
                                  WorkoutRepository workoutRepository,
                                  WorkoutExerciseRepository workoutExerciseRepository,
                                  PlannedSetRepository plannedSetRepository,
                                  ExerciseRepository exerciseRepository,
                                  FriendshipService friendshipService,
                                  ProgressionService progressionService,
                                  LoadingSchemeFactory loadingSchemeFactory) {
        this.workoutLogRepository = workoutLogRepository;
        this.loggedExerciseRepository = loggedExerciseRepository;
        this.loggedSetRepository = loggedSetRepository;
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.plannedSetRepository = plannedSetRepository;
        this.exerciseRepository = exerciseRepository;
        this.friendshipService = friendshipService;
        this.progressionService = progressionService;
        this.loadingSchemeFactory = loadingSchemeFactory;
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
                    .tracksWeight(SetTracking.tracksWeight(workoutExercise.getTracksWeight()))
                    .tracksDuration(SetTracking.tracksDuration(workoutExercise.getTracksDuration()))
                    .tracksDistance(SetTracking.tracksDistance(workoutExercise.getTracksDistance()))
                    .limbPattern(Laterality.pattern(workoutExercise.getExerciseEntity().getLimbPattern()))
                    .independentLoads(Laterality.independentLoads(
                            workoutExercise.getExerciseEntity().getIndependentLoads(),
                            workoutExercise.getExerciseEntity().getLoadingType()))
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
                        Float weight = null;
                        if (SetTracking.tracksWeight(workoutExercise.getTracksWeight())) {
                            weight = suggestion.targetWeight() != null
                                    ? Float.valueOf(suggestion.targetWeight().floatValue())
                                    : plannedSet.getTargetWeight();
                        }
                        boolean unilateral = Laterality.isUnilateral(workoutExercise.getExerciseEntity().getLimbPattern());
                        Integer rightReps = null;
                        Float rightWeight = null;
                        if (unilateral) {
                            rightReps = plannedSet.getRightReps() != null ? plannedSet.getRightReps() : reps;
                            if (SetTracking.tracksWeight(workoutExercise.getTracksWeight())) {
                                rightWeight = plannedSet.getRightWeight() != null
                                        ? plannedSet.getRightWeight()
                                        : weight;
                            }
                        }
                        weight = loadingSchemeFactory.snapWeight(workoutExercise.getExerciseEntity(), weight);
                        rightWeight = loadingSchemeFactory.snapWeight(workoutExercise.getExerciseEntity(), rightWeight);
                        return LoggedSetEntity.builder()
                                .loggedExerciseEntity(loggedExercise)
                                .setNumber(plannedSet.getSetNumber())
                                .actualReps(reps)
                                .actualWeight(weight)
                                .rightReps(rightReps)
                                .rightWeight(rightWeight)
                                .targetReps(reps)
                                .targetWeight(weight)
                                .actualDurationSeconds(SetTracking.tracksDuration(workoutExercise.getTracksDuration())
                                        ? plannedSet.getTargetDurationSeconds()
                                        : null)
                                .actualDistance(SetTracking.tracksDistance(workoutExercise.getTracksDistance())
                                        ? plannedSet.getTargetDistance()
                                        : null)
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
    public Page<WorkoutLogEntity> findAllForUser(UUID userId, String query, Pageable pageable) {
        Pageable sorted = withDefaultSort(pageable, "startedAt");
        if (query == null || query.isBlank()) {
            return workoutLogRepository.findByCreatedByUserId(userId, sorted);
        }
        return workoutLogRepository.findByCreatedByUserIdAndNameContainingIgnoreCase(userId, query.trim(), sorted);
    }

    @Override
    public Optional<WorkoutLogEntity> findInProgressForUser(UUID userId) {
        return workoutLogRepository.findFirstByCreatedByUserIdAndCompletedAtIsNullOrderByStartedAtDesc(userId);
    }

    @Override
    public Optional<WorkoutLogEntity> findOneForUser(Long id, UUID userId) {
        return workoutLogRepository.findByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public Optional<WorkoutLogEntity> findVisibleToUser(Long id, UUID userId) {
        Optional<WorkoutLogEntity> found = workoutLogRepository.findById(id);
        if (found.isEmpty()) {
            return found;
        }
        WorkoutLogEntity log = found.get();
        if (log.getCreatedByUserId().equals(userId)) {
            return found;
        }
        if (log.getCompletedAt() != null && friendshipService.isFriend(userId, log.getCreatedByUserId())) {
            return found;
        }
        return Optional.empty();
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
                .tracksWeight(SetTracking.tracksWeight(exercise.getTracksWeight()))
                .tracksDuration(SetTracking.tracksDuration(exercise.getTracksDuration()))
                .tracksDistance(SetTracking.tracksDistance(exercise.getTracksDistance()))
                .limbPattern(Laterality.pattern(exercise.getLimbPattern()))
                .independentLoads(Laterality.independentLoads(exercise.getIndependentLoads(), exercise.getLoadingType()))
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
        Integer duration = request != null ? request.getActualDurationSeconds() : null;
        Float distance = request != null ? request.getActualDistance() : null;
        Integer rightReps = request != null ? request.getRightReps() : null;
        Float rightWeight = request != null ? request.getRightWeight() : null;
        String notes = request != null ? request.getNotes() : null;

        if (reps == null || (SetTracking.tracksWeight(loggedExercise.getTracksWeight()) && weight == null)) {
            Recommendation suggestion = progressionService.forExercise(
                    loggedExercise.getWorkoutLogEntity().getCreatedByUserId(),
                    loggedExercise.getExerciseEntity());
            if (reps == null) {
                reps = suggestion.targetReps();
            }
            if (weight == null && suggestion.targetWeight() != null
                    && SetTracking.tracksWeight(loggedExercise.getTracksWeight())) {
                weight = suggestion.targetWeight().floatValue();
            }
        }

        if (Laterality.isUnilateral(loggedExercise.getLimbPattern())) {
            if (rightReps == null) {
                rightReps = reps;
            }
            if (rightWeight == null && SetTracking.tracksWeight(loggedExercise.getTracksWeight())) {
                rightWeight = weight;
            }
        }

        weight = loadingSchemeFactory.snapWeight(loggedExercise.getExerciseEntity(), weight);
        rightWeight = loadingSchemeFactory.snapWeight(loggedExercise.getExerciseEntity(), rightWeight);

        LoggedSetEntity loggedSet = LoggedSetEntity.builder()
                .loggedExerciseEntity(loggedExercise)
                .setNumber((int) (loggedSetRepository.countByLoggedExerciseEntity_Id(loggedExerciseId) + 1))
                .actualReps(reps)
                .actualWeight(weight)
                .rightReps(rightReps)
                .rightWeight(rightWeight)
                .targetReps(reps)
                .targetWeight(weight)
                .actualDurationSeconds(duration)
                .actualDistance(distance)
                .notes(notes)
                .failed(request != null && Boolean.TRUE.equals(request.getFailed()))
                .rightFailed(request != null && Boolean.TRUE.equals(request.getRightFailed()))
                .loggedAt(null)
                .build();

        return loggedSetRepository.save(loggedSet);
    }

    @Override
    @Transactional
    public LoggedSetEntity updateLoggedSet(Long setId, LoggedSetRequest request) {
        return loggedSetRepository.findById(setId).map(existingSet -> {
            ExerciseEntity exercise = existingSet.getLoggedExerciseEntity().getExerciseEntity();
            Optional.ofNullable(request.getActualReps()).ifPresent(existingSet::setActualReps);
            Optional.ofNullable(request.getActualWeight())
                    .ifPresent(weight -> existingSet.setActualWeight(loadingSchemeFactory.snapWeight(exercise, weight)));
            Optional.ofNullable(request.getRightReps()).ifPresent(existingSet::setRightReps);
            Optional.ofNullable(request.getRightWeight())
                    .ifPresent(weight -> existingSet.setRightWeight(loadingSchemeFactory.snapWeight(exercise, weight)));
            Optional.ofNullable(request.getActualDurationSeconds()).ifPresent(existingSet::setActualDurationSeconds);
            Optional.ofNullable(request.getActualDistance()).ifPresent(existingSet::setActualDistance);
            Optional.ofNullable(request.getNotes()).ifPresent(existingSet::setNotes);
            Optional.ofNullable(request.getFailed()).ifPresent(existingSet::setFailed);
            Optional.ofNullable(request.getRightFailed()).ifPresent(existingSet::setRightFailed);
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
        return workoutLogRepository.findFriendsFeed(userId, friendIds, withDefaultSort(pageable, "completedAt"));
    }

    private static Pageable withDefaultSort(Pageable pageable, String property) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, property));
    }

    @Override
    public Map<Long, List<String>> exerciseNamesByLogId(Collection<Long> logIds) {
        if (logIds == null || logIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> names = new LinkedHashMap<>();
        for (LoggedExerciseRepository.LogExerciseName row : loggedExerciseRepository.findExerciseNamesForLogs(logIds)) {
            names.computeIfAbsent(row.getWorkoutLogId(), unused -> new ArrayList<>()).add(row.getName());
        }
        return names;
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
