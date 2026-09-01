package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workout.UpdateWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.*;
import com.lukeroche.fit.repositories.*;
import com.lukeroche.fit.services.WorkoutService;
import com.lukeroche.fit.services.progression.LoadingTypeSuggestion;
import com.lukeroche.fit.services.progression.LoadingSchemeFactory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for {@link WorkoutService}. Planned weights are snapped to the
 * exercise load step. Copy-to-library is a deep copy of log rows into a new
 * template owned by the copier.
 */
@Service
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final PlannedSetRepository plannedSetRepository;
    private WorkoutRepository workoutRepository;

    private ExerciseRepository exerciseRepository;

    private final WorkoutLogRepository workoutLogRepository;
    private final LoggedExerciseRepository loggedExerciseRepository;
    private final LoadingTypeSuggestion loadingTypeSuggestion;
    private final LoggedSetRepository loggedSetRepository;
    private final LoadingSchemeFactory loadingSchemeFactory;

    public WorkoutServiceImpl(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository,
                              WorkoutExerciseRepository workoutExerciseRepository, PlannedSetRepository plannedSetRepository,
                              WorkoutLogRepository workoutLogRepository, LoggedExerciseRepository loggedExerciseRepository,
                              LoggedSetRepository loggedSetRepository, LoadingTypeSuggestion loadingTypeSuggestion,
                              LoadingSchemeFactory loadingSchemeFactory) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseRepository = exerciseRepository;
        this.plannedSetRepository = plannedSetRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.loggedExerciseRepository = loggedExerciseRepository;
        this.loggedSetRepository = loggedSetRepository;
        this.loadingTypeSuggestion = loadingTypeSuggestion;
        this.loadingSchemeFactory = loadingSchemeFactory;
    }

    @Override
    public WorkoutEntity save(WorkoutEntity workoutEntity) {
        return workoutRepository.save(workoutEntity);
    }

    @Override
    public Page<WorkoutEntity> findAllForUser(UUID userId, String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return workoutRepository.findByCreatedByUserId(userId, pageable);
        }
        return workoutRepository.findByCreatedByUserIdAndNameContainingIgnoreCase(userId, query.trim(), pageable);
    }

    @Override
    public Optional<WorkoutEntity> findOneForUser(Long id, UUID userId) {
        return workoutRepository.findByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public boolean isOwnedByUser(Long id, UUID userId) {
        return workoutRepository.existsByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public WorkoutEntity partialUpdate(Long id, UUID userId, WorkoutEntity workoutEntity) {
        workoutEntity.setId(id);

        return workoutRepository.findByIdAndCreatedByUserId(id, userId).map(existingWorkout -> {
            Optional.ofNullable(workoutEntity.getName()).ifPresent((existingWorkout::setName));
            Optional.ofNullable(workoutEntity.getDescription()).ifPresent((existingWorkout::setDescription));
            return workoutRepository.save(existingWorkout);
        }).orElseThrow(() -> new EntityNotFoundException("Workout does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }


    @Override
    public WorkoutExerciseEntity addWorkoutExercise(Long workoutId, UUID userId, AddWorkoutExerciseRequest request){

        WorkoutEntity workout = workoutRepository.findByIdAndCreatedByUserId(workoutId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Workout does not exist"));

        ExerciseEntity exercise = exerciseRepository.findByIdAndCreatedByUserId(request.getExerciseId(), userId)
                .orElseThrow(() -> new EntityNotFoundException("Exercise does not exist"));

        int minReps = request.getMinReps() == null ? 6 : request.getMinReps();
        int maxReps = request.getMaxReps() == null ? 12 : request.getMaxReps();
        if (minReps > maxReps) {
            int swap = minReps;
            minReps = maxReps;
            maxReps = swap;
        }

        WorkoutExerciseEntity workoutExercise = WorkoutExerciseEntity.builder()
                .workoutEntity(workout)
                .exerciseEntity(exercise)
                .orderIndex(workoutExerciseRepository.countByWorkoutEntity_Id(workoutId) + 1)
                .minReps(minReps)
                .maxReps(maxReps)
                .tracksWeight(SetTracking.resolveWeight(request.getTracksWeight(), exercise.getTracksWeight()))
                .tracksDuration(SetTracking.resolveDuration(request.getTracksDuration(), exercise.getTracksDuration()))
                .tracksDistance(SetTracking.resolveDistance(request.getTracksDistance(), exercise.getTracksDistance()))
                .limbPattern(Laterality.resolvePattern(request.getLimbPattern(), exercise.getLimbPattern()))
                .independentLoads(Laterality.resolveIndependentLoads(
                        request.getIndependentLoads(),
                        exercise.getIndependentLoads(),
                        exercise.getLoadingType()))
                .build();

        return workoutExerciseRepository.save(workoutExercise);
    }

    @Override
    @Transactional
    public WorkoutExerciseEntity updateWorkoutExercise(Long workoutId, Long workoutExerciseId, UpdateWorkoutExerciseRequest workoutExerciseRequest) {

        List<WorkoutExerciseEntity> workoutExercises = workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(workoutId);

        WorkoutExerciseEntity reorderedExercise = workoutExercises.stream()
                .filter(workoutExerciseEntity -> workoutExerciseEntity.getId().equals(workoutExerciseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Workout exercise does not exist"));

        Optional.ofNullable(workoutExerciseRequest.getMinReps()).ifPresent(reorderedExercise::setMinReps);
        Optional.ofNullable(workoutExerciseRequest.getMaxReps()).ifPresent(reorderedExercise::setMaxReps);
        Optional.ofNullable(workoutExerciseRequest.getTracksWeight()).ifPresent(reorderedExercise::setTracksWeight);
        Optional.ofNullable(workoutExerciseRequest.getTracksDuration()).ifPresent(reorderedExercise::setTracksDuration);
        Optional.ofNullable(workoutExerciseRequest.getTracksDistance()).ifPresent(reorderedExercise::setTracksDistance);
        Optional.ofNullable(workoutExerciseRequest.getLimbPattern()).ifPresent(reorderedExercise::setLimbPattern);
        Optional.ofNullable(workoutExerciseRequest.getIndependentLoads()).ifPresent(reorderedExercise::setIndependentLoads);

        if (workoutExerciseRequest.getOrderIndex() == null) {
            return workoutExerciseRepository.save(reorderedExercise);
        }

        // Clients send 1-based order; the list is 0-based after removing the row.
        int newIndex = Math.toIntExact(workoutExerciseRequest.getOrderIndex()) - 1;


        workoutExercises.remove(reorderedExercise);

        newIndex = Math.max(0, Math.min(newIndex, workoutExercises.size()));

        workoutExercises.add(newIndex, reorderedExercise);

        for (int i = 0; i < workoutExercises.size(); i++) {
            workoutExercises.get(i).setOrderIndex((long) i+1);
        }

        workoutExerciseRepository.saveAll(workoutExercises);

        return reorderedExercise;



    }

    @Override
    @Transactional
    public void removeExerciseFromWorkout(Long workoutId, Long workoutExerciseId) {

        workoutExerciseRepository.deleteById(workoutExerciseId);

        List<WorkoutExerciseEntity> workoutExercises = workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(workoutId);

        for (int i = 0; i < workoutExercises.size(); i++) {
            workoutExercises.get(i).setOrderIndex((long) i+1);
        }

        workoutExerciseRepository.saveAll(workoutExercises);

    }

    @Override
    public boolean workoutExerciseBelongsToWorkout(Long workoutExerciseId, Long workoutId) {
        return workoutExerciseRepository.existsByIdAndWorkoutEntity_Id(workoutExerciseId, workoutId);
    }

    @Override
    @Transactional
    public PlannedSetEntity addPlannedSet(Long workoutExerciseId, PlannedSetRequest request) {
        WorkoutExerciseEntity workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new EntityNotFoundException("Workout exercise does not exist"));

        PlannedSetEntity plannedSet = PlannedSetEntity.builder()
                .workoutExerciseEntity(workoutExercise)
                .setNumber((int) (plannedSetRepository.countByWorkoutExerciseEntity_Id(workoutExerciseId) + 1))
                .targetReps(request.getTargetReps())
                .targetWeight(loadingSchemeFactory.snapWeight(workoutExercise.getExerciseEntity(), request.getTargetWeight()))
                .rightReps(request.getRightReps())
                .rightWeight(loadingSchemeFactory.snapWeight(workoutExercise.getExerciseEntity(), request.getRightWeight()))
                .targetDurationSeconds(request.getTargetDurationSeconds())
                .targetDistance(request.getTargetDistance())
                .restTimeSeconds(request.getRestTimeSeconds())
                .build();

        return plannedSetRepository.save(plannedSet);
    }

    @Override
    @Transactional
    public PlannedSetEntity updatePlannedSet(Long setId, PlannedSetRequest request) {
        return plannedSetRepository.findById(setId).map(existingSet -> {
            ExerciseEntity exercise = existingSet.getWorkoutExerciseEntity().getExerciseEntity();
            Optional.ofNullable(request.getTargetReps()).ifPresent(existingSet::setTargetReps);
            Optional.ofNullable(request.getTargetWeight())
                    .ifPresent(weight -> existingSet.setTargetWeight(loadingSchemeFactory.snapWeight(exercise, weight)));
            Optional.ofNullable(request.getRightReps()).ifPresent(existingSet::setRightReps);
            Optional.ofNullable(request.getRightWeight())
                    .ifPresent(weight -> existingSet.setRightWeight(loadingSchemeFactory.snapWeight(exercise, weight)));
            Optional.ofNullable(request.getTargetDurationSeconds()).ifPresent(existingSet::setTargetDurationSeconds);
            Optional.ofNullable(request.getTargetDistance()).ifPresent(existingSet::setTargetDistance);
            Optional.ofNullable(request.getRestTimeSeconds()).ifPresent(existingSet::setRestTimeSeconds);
            return plannedSetRepository.save(existingSet);
        }).orElseThrow(() -> new EntityNotFoundException("Planned set does not exist"));
    }

    @Override
    @Transactional
    public void removePlannedSet(Long workoutExerciseId, Long setId) {
        plannedSetRepository.deleteById(setId);

        List<PlannedSetEntity> remaining = plannedSetRepository.findByWorkoutExerciseEntity_IdOrderBySetNumberAsc(workoutExerciseId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSetNumber(i + 1);
        }
        plannedSetRepository.saveAll(remaining);
    }

    @Override
    public boolean plannedSetBelongsToWorkoutExercise(Long setId, Long workoutExerciseId) {
        return plannedSetRepository.existsByIdAndWorkoutExerciseEntity_Id(setId, workoutExerciseId);
    }

    // Copies produce independent workout/set rows (no FK back to the source log). Exercises are reused
    // from the copier's library when the name already exists, otherwise a new library row is created.
    // If the friend later renames/deletes their exercise or the source log, this copy is unaffected.
    @Override
    @Transactional
    public WorkoutEntity copyWorkoutLogToLibrary(Long workoutLogId, UUID copyingUserId) {
        WorkoutLogEntity sourceLog = workoutLogRepository.findById(workoutLogId)
                .orElseThrow(() -> new EntityNotFoundException("Workout log does not exist"));

        WorkoutEntity newWorkout = workoutRepository.save(WorkoutEntity.builder()
                .createdByUserId(copyingUserId)
                .name(uniqueWorkoutName(sourceLog.getName(), copyingUserId))
                .description("Copied from " + sourceLog.getName())
                .build());

        List<LoggedExerciseEntity> sourceLoggedExercises =
                loggedExerciseRepository.findByWorkoutLogEntity_IdOrderByOrderIndexAsc(workoutLogId);

        Map<Long, WorkoutExerciseEntity> sourceRangesByExerciseId = sourceRanges(sourceLog.getSourceWorkoutId());
        Map<Long, ExerciseEntity> exercisesBySourceId = new HashMap<>();

        for (LoggedExerciseEntity loggedExercise : sourceLoggedExercises) {
            ExerciseEntity sourceExercise = loggedExercise.getExerciseEntity();
            ExerciseEntity libraryExercise = exercisesBySourceId.computeIfAbsent(
                    sourceExercise.getId(),
                    unused -> resolveLibraryExercise(sourceExercise, copyingUserId));

            List<LoggedSetEntity> sourceSets =
                    loggedSetRepository.findByLoggedExerciseEntity_IdOrderBySetNumberAsc(loggedExercise.getId());
            int[] range = resolveRepRange(
                    sourceRangesByExerciseId.get(sourceExercise.getId()),
                    sourceSets);

            WorkoutExerciseEntity newWorkoutExercise = workoutExerciseRepository.save(WorkoutExerciseEntity.builder()
                    .workoutEntity(newWorkout)
                    .exerciseEntity(libraryExercise)
                    .orderIndex(loggedExercise.getOrderIndex())
                    .minReps(range[0])
                    .maxReps(range[1])
                    .tracksWeight(SetTracking.resolveWeight(loggedExercise.getTracksWeight(), sourceExercise.getTracksWeight()))
                    .tracksDuration(SetTracking.resolveDuration(loggedExercise.getTracksDuration(), sourceExercise.getTracksDuration()))
                    .tracksDistance(SetTracking.resolveDistance(loggedExercise.getTracksDistance(), sourceExercise.getTracksDistance()))
                    .limbPattern(Laterality.resolvePattern(loggedExercise.getLimbPattern(), sourceExercise.getLimbPattern()))
                    .independentLoads(Laterality.resolveIndependentLoads(
                            loggedExercise.getIndependentLoads(),
                            sourceExercise.getIndependentLoads(),
                            sourceExercise.getLoadingType()))
                    .build());

            List<PlannedSetEntity> newPlannedSets = sourceSets.stream()
                    .map(loggedSet -> PlannedSetEntity.builder()
                            .workoutExerciseEntity(newWorkoutExercise)
                            .setNumber(loggedSet.getSetNumber())
                            .targetReps(loggedSet.getActualReps())
                            .targetWeight(loggedSet.getActualWeight())
                            .rightReps(loggedSet.getRightReps())
                            .rightWeight(loggedSet.getRightWeight())
                            .targetDurationSeconds(loggedSet.getActualDurationSeconds())
                            .targetDistance(loggedSet.getActualDistance())
                            .restTimeSeconds(null)
                            .build())
                    .toList();
            plannedSetRepository.saveAll(newPlannedSets);

            newWorkoutExercise.setPlannedSets(newPlannedSets);
            newWorkout.getWorkoutExercises().add(newWorkoutExercise);
        }

        return newWorkout;
    }

    private ExerciseEntity resolveLibraryExercise(ExerciseEntity sourceExercise, UUID copyingUserId) {
        return exerciseRepository
                .findFirstByCreatedByUserIdAndNameIgnoreCase(copyingUserId, sourceExercise.getName())
                .orElseGet(() -> {
                    ExerciseEntity copy = ExerciseEntity.builder()
                            .name(sourceExercise.getName())
                            .description(sourceExercise.getDescription())
                            .createdByUserId(copyingUserId)
                            .loadingType(sourceExercise.getLoadingType())
                            .loadStep(sourceExercise.getLoadStep())
                            .tracksWeight(SetTracking.tracksWeight(sourceExercise.getTracksWeight()))
                            .tracksDuration(SetTracking.tracksDuration(sourceExercise.getTracksDuration()))
                            .tracksDistance(SetTracking.tracksDistance(sourceExercise.getTracksDistance()))
                            .limbPattern(Laterality.pattern(sourceExercise.getLimbPattern()))
                            .independentLoads(Laterality.independentLoads(
                                    sourceExercise.getIndependentLoads(),
                                    sourceExercise.getLoadingType()))
                            .build();
                    loadingTypeSuggestion.applyDefaults(copy);
                    return exerciseRepository.save(copy);
                });
    }

    private String uniqueWorkoutName(String baseName, UUID copyingUserId) {
        if (!workoutRepository.existsByCreatedByUserIdAndNameIgnoreCase(copyingUserId, baseName)) {
            return baseName;
        }
        int suffix = 2;
        String candidate;
        do {
            candidate = baseName + "(" + suffix + ")";
            suffix++;
        } while (workoutRepository.existsByCreatedByUserIdAndNameIgnoreCase(copyingUserId, candidate));
        return candidate;
    }

    private Map<Long, WorkoutExerciseEntity> sourceRanges(Long sourceWorkoutId) {
        if (sourceWorkoutId == null) {
            return Map.of();
        }
        Map<Long, WorkoutExerciseEntity> byExerciseId = new HashMap<>();
        for (WorkoutExerciseEntity workoutExercise :
                workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(sourceWorkoutId)) {
            byExerciseId.putIfAbsent(workoutExercise.getExerciseEntity().getId(), workoutExercise);
        }
        return byExerciseId;
    }

    private static int[] resolveRepRange(WorkoutExerciseEntity sourceExercise, List<LoggedSetEntity> sets) {
        // Prefer the source template's min/max so a copy keeps the programmed
        // range even when the session went outside it.
        if (sourceExercise != null && sourceExercise.getMinReps() != null && sourceExercise.getMaxReps() != null) {
            int min = sourceExercise.getMinReps();
            int max = sourceExercise.getMaxReps();
            return min <= max ? new int[]{min, max} : new int[]{max, min};
        }
        Integer min = null;
        Integer max = null;
        for (LoggedSetEntity set : sets) {
            Integer reps = set.getTargetReps() != null ? set.getTargetReps() : set.getActualReps();
            if (reps == null || reps <= 0) {
                continue;
            }
            min = min == null ? reps : Math.min(min, reps);
            max = max == null ? reps : Math.max(max, reps);
        }
        if (min == null) {
            return new int[]{6, 12};
        }
        return new int[]{min, max};
    }
}
