/*
 * Filename: WorkoutLogServiceImpl.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.workoutlog.AddLoggedExerciseRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.StartSessionRequest;
import com.lukeroche.fit.domain.entities.*;
import com.lukeroche.fit.repositories.*;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.WorkoutLogService;
import com.lukeroche.fit.services.progression.LoadingSchemeFactory;
import com.lukeroche.fit.services.progression.ProgressionConfig;
import com.lukeroche.fit.services.progression.ProgressionService;
import com.lukeroche.fit.services.progression.Recommendation;
import com.lukeroche.fit.services.progression.SessionBest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for {@link WorkoutLogService}. Starting a session clones the
 * workout structure, then seeds actuals from progression (or a recent miss on
 * this template) while targets stay the programmed prescription.
 */
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
    private final ProgressionConfig progressionConfig;

    public WorkoutLogServiceImpl(WorkoutLogRepository workoutLogRepository,
                                  LoggedExerciseRepository loggedExerciseRepository,
                                  LoggedSetRepository loggedSetRepository,
                                  WorkoutRepository workoutRepository,
                                  WorkoutExerciseRepository workoutExerciseRepository,
                                  PlannedSetRepository plannedSetRepository,
                                  ExerciseRepository exerciseRepository,
                                  FriendshipService friendshipService,
                                  ProgressionService progressionService,
                                  LoadingSchemeFactory loadingSchemeFactory,
                                  ProgressionConfig progressionConfig) {
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
        this.progressionConfig = progressionConfig;
    }

    @Override
    @Transactional
    public WorkoutLogEntity startSession(Long sourceWorkoutId, UUID userId, StartSessionRequest request) {
        WorkoutEntity sourceWorkout = workoutRepository
                .findByIdAndCreatedByUserId(sourceWorkoutId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workout does not exist"));

        if (workoutLogRepository.findFirstByCreatedByUserIdAndCompletedAtIsNullOrderByStartedAtDesc(userId)
                .isPresent()) {
            throw new IllegalStateException("You already have a session in progress");
        }

        WorkoutLogEntity workoutLog = workoutLogRepository.save(WorkoutLogEntity.builder()
                .createdByUserId(userId)
                .sourceWorkoutId(sourceWorkoutId)
                .name(request != null && request.getName() != null ? request.getName() : sourceWorkout.getName())
                .startedAt(LocalDateTime.now())
                .completedAt(null)
                .build());

        List<WorkoutExerciseEntity> sourceExercises =
                workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(sourceWorkoutId);
        LastWorkoutSnapshot lastWorkout = lastCompletedWorkout(userId, sourceWorkoutId);

        for (WorkoutExerciseEntity workoutExercise : sourceExercises) {
            ExerciseEntity exercise = workoutExercise.getExerciseEntity();
            LoggedExerciseEntity loggedExercise = loggedExerciseRepository.save(LoggedExerciseEntity.builder()
                    .workoutLogEntity(workoutLog)
                    .exerciseEntity(exercise)
                    .orderIndex(workoutExercise.getOrderIndex())
                    .tracksWeight(SetTracking.tracksWeight(workoutExercise.getTracksWeight()))
                    .tracksDuration(SetTracking.tracksDuration(workoutExercise.getTracksDuration()))
                    .tracksDistance(SetTracking.tracksDistance(workoutExercise.getTracksDistance()))
                    .limbPattern(Laterality.pattern(exercise.getLimbPattern()))
                    .independentLoads(Laterality.independentLoads(
                            exercise.getIndependentLoads(),
                            exercise.getLoadingType()))
                    .build());

            List<PlannedSetEntity> plannedSets =
                    plannedSetRepository.findByWorkoutExerciseEntity_IdOrderBySetNumberAsc(workoutExercise.getId());

            Recommendation suggestion = progressionService.forExercise(
                    userId,
                    exercise,
                    workoutExercise.getMinReps(),
                    workoutExercise.getMaxReps());

            Integer prescriptionReps = suggestion.targetReps() != null
                    ? suggestion.targetReps()
                    : (workoutExercise.getMinReps() != null ? workoutExercise.getMinReps() : 6);
            Float prescriptionWeight = null;
            if (SetTracking.tracksWeight(workoutExercise.getTracksWeight()) && suggestion.targetWeight() != null) {
                prescriptionWeight = loadingSchemeFactory.snapWeight(
                        exercise, suggestion.targetWeight().floatValue());
            }

            List<LoggedSetEntity> lastSets = lastWorkout.setsFor(exercise.getId());
            boolean replayMiss = shouldReplay(lastSets, workoutExercise.getMinReps(), workoutExercise.getMaxReps());
            Map<Integer, LoggedSetEntity> lastByNumber = indexBySetNumber(lastSets);

            List<LoggedSetEntity> loggedSets = new ArrayList<>();
            for (PlannedSetEntity plannedSet : plannedSets) {
                loggedSets.add(seedLoggedSet(
                        loggedExercise,
                        workoutExercise,
                        plannedSet,
                        prescriptionReps,
                        prescriptionWeight,
                        replayMiss ? lastByNumber.get(plannedSet.getSetNumber()) : null,
                        replayMiss));
            }
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
        }).orElseThrow(() -> new EntityNotFoundException("Workout log does not exist"));
    }

    @Override
    public WorkoutLogEntity finishSession(Long id, UUID userId) {
        return workoutLogRepository.findByIdAndCreatedByUserId(id, userId).map(existingLog -> {
            existingLog.setCompletedAt(LocalDateTime.now());
            return workoutLogRepository.save(existingLog);
        }).orElseThrow(() -> new EntityNotFoundException("Workout log does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutLogRepository.deleteById(id);
    }

    @Override
    public LoggedExerciseEntity addLoggedExercise(Long workoutLogId, UUID userId, AddLoggedExerciseRequest request) {
        WorkoutLogEntity workoutLog = workoutLogRepository.findByIdAndCreatedByUserId(workoutLogId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workout log does not exist"));

        ExerciseEntity exercise = exerciseRepository.findByIdAndCreatedByUserId(request.getExerciseId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Exercise does not exist"));

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
        LoggedExerciseEntity loggedExercise = loggedExerciseRepository.findById(loggedExerciseId)
                .orElseThrow(() -> new EntityNotFoundException("Logged exercise does not exist"));

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
        }).orElseThrow(() -> new EntityNotFoundException("Logged set does not exist"));
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

    /**
     * Last finished log of this workout, only when it is still inside the
     * replay window ({@code holdAfterDays}). Older logs are ignored so
     * hold/deload/1RM can take over.
     */
    private LastWorkoutSnapshot lastCompletedWorkout(UUID userId, Long sourceWorkoutId) {
        Optional<WorkoutLogEntity> found = workoutLogRepository
                .findFirstByCreatedByUserIdAndSourceWorkoutIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(
                        userId, sourceWorkoutId);
        if (found.isEmpty() || found.get().getCompletedAt() == null) {
            return LastWorkoutSnapshot.none();
        }
        WorkoutLogEntity last = found.get();
        int holdAfterDays = progressionConfig.holdAfterDays();
        if (holdAfterDays <= 0) {
            return LastWorkoutSnapshot.none();
        }
        long daysOff = ChronoUnit.DAYS.between(last.getCompletedAt().toLocalDate(), LocalDate.now());
        if (daysOff > holdAfterDays) {
            return LastWorkoutSnapshot.none();
        }

        Map<Long, List<LoggedSetEntity>> byExercise = new HashMap<>();
        for (LoggedExerciseEntity loggedExercise :
                loggedExerciseRepository.findByWorkoutLogEntity_IdOrderByOrderIndexAsc(last.getId())) {
            byExercise.put(
                    loggedExercise.getExerciseEntity().getId(),
                    loggedSetRepository.findByLoggedExerciseEntity_IdOrderBySetNumberAsc(loggedExercise.getId()));
        }
        return new LastWorkoutSnapshot(byExercise);
    }

    /**
     * Replay the last log's actuals when that exercise missed on the same
     * ladder. Hits, range changes, and exercises no longer on the workout
     * fall through to the suggestion instead.
     */
    private static boolean shouldReplay(List<LoggedSetEntity> lastSets, Integer minReps, Integer maxReps) {
        if (lastSets == null || lastSets.isEmpty()) {
            return false;
        }
        if (!sameLadder(lastSets, minReps, maxReps)) {
            return false;
        }
        return !SessionBest.prescriptionHit(toHistoryRows(lastSets, 0L, null));
    }

    private static boolean sameLadder(List<LoggedSetEntity> lastSets, Integer minReps, Integer maxReps) {
        int lo = minReps == null ? 6 : minReps;
        int hi = maxReps == null ? 12 : maxReps;
        if (lo > hi) {
            int swap = lo;
            lo = hi;
            hi = swap;
        }
        for (LoggedSetEntity set : lastSets) {
            Integer target = set.getTargetReps();
            if (target != null && target > 0 && (target < lo || target > hi)) {
                return false;
            }
        }
        return true;
    }

    private LoggedSetEntity seedLoggedSet(LoggedExerciseEntity loggedExercise,
                                          WorkoutExerciseEntity workoutExercise,
                                          PlannedSetEntity plannedSet,
                                          Integer prescriptionReps,
                                          Float prescriptionWeight,
                                          LoggedSetEntity prior,
                                          boolean replayMiss) {
        ExerciseEntity exercise = workoutExercise.getExerciseEntity();
        boolean tracksWeight = SetTracking.tracksWeight(workoutExercise.getTracksWeight());
        boolean unilateral = Laterality.isUnilateral(exercise.getLimbPattern());

        Integer actualReps = prescriptionReps;
        Float actualWeight = prescriptionWeight;
        Integer rightReps = unilateral ? prescriptionReps : null;
        Float rightWeight = unilateral && tracksWeight ? prescriptionWeight : null;

        if (replayMiss) {
            if (prior == null) {
                actualReps = null;
                actualWeight = null;
                rightReps = null;
                rightWeight = null;
            } else {
                actualReps = prior.getActualReps();
                actualWeight = tracksWeight ? prior.getActualWeight() : null;
                rightReps = unilateral ? prior.getRightReps() : null;
                rightWeight = unilateral && tracksWeight ? prior.getRightWeight() : null;
            }
        }

        actualWeight = loadingSchemeFactory.snapWeight(exercise, actualWeight);
        rightWeight = loadingSchemeFactory.snapWeight(exercise, rightWeight);

        return LoggedSetEntity.builder()
                .loggedExerciseEntity(loggedExercise)
                .setNumber(plannedSet.getSetNumber())
                .actualReps(actualReps)
                .actualWeight(actualWeight)
                .rightReps(rightReps)
                .rightWeight(rightWeight)
                .targetReps(prescriptionReps)
                .targetWeight(prescriptionWeight)
                .actualDurationSeconds(SetTracking.tracksDuration(workoutExercise.getTracksDuration())
                        ? plannedSet.getTargetDurationSeconds()
                        : null)
                .actualDistance(SetTracking.tracksDistance(workoutExercise.getTracksDistance())
                        ? plannedSet.getTargetDistance()
                        : null)
                .loggedAt(null)
                .build();
    }

    private static Map<Integer, LoggedSetEntity> indexBySetNumber(List<LoggedSetEntity> sets) {
        if (sets == null || sets.isEmpty()) {
            return Map.of();
        }
        Map<Integer, LoggedSetEntity> byNumber = new HashMap<>();
        for (LoggedSetEntity set : sets) {
            if (set.getSetNumber() != null) {
                byNumber.put(set.getSetNumber(), set);
            }
        }
        return byNumber;
    }

    private static List<SetHistoryRow> toHistoryRows(List<LoggedSetEntity> sets,
                                                     Long workoutLogId,
                                                     LocalDateTime completedAt) {
        List<SetHistoryRow> rows = new ArrayList<>();
        for (LoggedSetEntity set : sets) {
            rows.add(new SetHistoryRow(
                    workoutLogId,
                    completedAt,
                    set.getSetNumber(),
                    set.getActualReps(),
                    set.getActualWeight(),
                    set.getRightReps(),
                    set.getRightWeight(),
                    set.getTargetReps(),
                    set.getTargetWeight(),
                    set.getFailed(),
                    set.getRightFailed()));
        }
        return rows;
    }

    private record LastWorkoutSnapshot(Map<Long, List<LoggedSetEntity>> setsByExerciseId) {

        static LastWorkoutSnapshot none() {
            return new LastWorkoutSnapshot(Map.of());
        }

        List<LoggedSetEntity> setsFor(Long exerciseId) {
            return setsByExerciseId.get(exerciseId);
        }
    }
}
