package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workout.UpdateWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.*;
import com.lukeroche.fit.repositories.*;
import com.lukeroche.fit.services.WorkoutService;
import com.lukeroche.fit.services.progression.LoadingTypeSuggestion;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public WorkoutServiceImpl(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository,
                              WorkoutExerciseRepository workoutExerciseRepository, PlannedSetRepository plannedSetRepository,
                              WorkoutLogRepository workoutLogRepository, LoggedExerciseRepository loggedExerciseRepository,
                              LoggedSetRepository loggedSetRepository, LoadingTypeSuggestion loadingTypeSuggestion) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseRepository = exerciseRepository;
        this.plannedSetRepository = plannedSetRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.loggedExerciseRepository = loggedExerciseRepository;
        this.loggedSetRepository = loggedSetRepository;
        this.loadingTypeSuggestion = loadingTypeSuggestion;
    }

    @Override
    public WorkoutEntity save(WorkoutEntity workoutEntity) {
        return workoutRepository.save(workoutEntity);
    }

    @Override
    public Page<WorkoutEntity> findAllForUser(UUID userId, Pageable pageable) {
        return workoutRepository.findByCreatedByUserId(userId, pageable);
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
            Optional.ofNullable(workoutEntity.getVisibility()).ifPresent((existingWorkout::setVisibility));
            return workoutRepository.save(existingWorkout);
        }).orElseThrow(() -> new RuntimeException("Workout does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }


    @Override
    public WorkoutExerciseEntity addWorkoutExercise(Long workoutId, UUID userId, AddWorkoutExerciseRequest request){

        WorkoutEntity workout = workoutRepository.findByIdAndCreatedByUserId(workoutId, userId).orElseThrow();

        ExerciseEntity exercise = exerciseRepository.findByIdAndCreatedByUserId(request.getExerciseId(), userId).orElseThrow();

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
                .build();

        return workoutExerciseRepository.save(workoutExercise);
    }

    @Override
    @Transactional
    public WorkoutExerciseEntity reorderWorkoutExercise(Long workoutId, Long workoutExerciseID, UpdateWorkoutExerciseRequest workoutExerciseRequest) {

        List<WorkoutExerciseEntity> workoutExercises = workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(workoutId);

        WorkoutExerciseEntity reorderedExercise = workoutExercises.stream()
                .filter(workoutExerciseEntity -> workoutExerciseEntity.getId().equals(workoutExerciseID))
                .findFirst()
                .orElseThrow();

        Optional.ofNullable(workoutExerciseRequest.getMinReps()).ifPresent(reorderedExercise::setMinReps);
        Optional.ofNullable(workoutExerciseRequest.getMaxReps()).ifPresent(reorderedExercise::setMaxReps);

        if (workoutExerciseRequest.getOrderIndex() == null) {
            return workoutExerciseRepository.save(reorderedExercise);
        }

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
    public PlannedSetEntity addPlannedSet(Long workoutExerciseId, PlannedSetRequest request) {
        WorkoutExerciseEntity workoutExercise = workoutExerciseRepository.findById(workoutExerciseId).orElseThrow();

        PlannedSetEntity plannedSet = PlannedSetEntity.builder()
                .workoutExerciseEntity(workoutExercise)
                .setNumber((int) (plannedSetRepository.countByWorkoutExerciseEntity_Id(workoutExerciseId) + 1))
                .targetReps(request.getTargetReps())
                .targetWeight(request.getTargetWeight())
                .targetDurationSeconds(request.getTargetDurationSeconds())
                .restTimeSeconds(request.getRestTimeSeconds())
                .build();

        return plannedSetRepository.save(plannedSet);
    }

    @Override
    public PlannedSetEntity updatePlannedSet(Long setId, PlannedSetRequest request) {
        return plannedSetRepository.findById(setId).map(existingSet -> {
            Optional.ofNullable(request.getTargetReps()).ifPresent(existingSet::setTargetReps);
            Optional.ofNullable(request.getTargetWeight()).ifPresent(existingSet::setTargetWeight);
            Optional.ofNullable(request.getTargetDurationSeconds()).ifPresent(existingSet::setTargetDurationSeconds);
            Optional.ofNullable(request.getRestTimeSeconds()).ifPresent(existingSet::setRestTimeSeconds);
            return plannedSetRepository.save(existingSet);
        }).orElseThrow(() -> new RuntimeException("Planned set does not exist"));
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
                .orElseThrow(() -> new RuntimeException("Workout log does not exist"));

        WorkoutEntity newWorkout = workoutRepository.save(WorkoutEntity.builder()
                .createdByUserId(copyingUserId)
                .name(uniqueWorkoutName(sourceLog.getName(), copyingUserId))
                .description("Copied from a friend's session")
                .visibility(false)
                .build());

        List<LoggedExerciseEntity> sourceLoggedExercises =
                loggedExerciseRepository.findByWorkoutLogEntity_IdOrderByOrderIndexAsc(workoutLogId);

        Map<Long, ExerciseEntity> exercisesBySourceId = new HashMap<>();

        for (LoggedExerciseEntity loggedExercise : sourceLoggedExercises) {
            ExerciseEntity sourceExercise = loggedExercise.getExerciseEntity();
            ExerciseEntity libraryExercise = exercisesBySourceId.computeIfAbsent(
                    sourceExercise.getId(),
                    unused -> resolveLibraryExercise(sourceExercise, copyingUserId));

            WorkoutExerciseEntity newWorkoutExercise = workoutExerciseRepository.save(WorkoutExerciseEntity.builder()
                    .workoutEntity(newWorkout)
                    .exerciseEntity(libraryExercise)
                    .orderIndex(loggedExercise.getOrderIndex())
                    .minReps(6)
                    .maxReps(12)
                    .build());

            List<PlannedSetEntity> newPlannedSets = loggedSetRepository
                    .findByLoggedExerciseEntity_IdOrderBySetNumberAsc(loggedExercise.getId()).stream()
                    .map(loggedSet -> PlannedSetEntity.builder()
                            .workoutExerciseEntity(newWorkoutExercise)
                            .setNumber(loggedSet.getSetNumber())
                            .targetReps(loggedSet.getActualReps())
                            .targetWeight(loggedSet.getActualWeight())
                            //.targetDurationSeconds(loggedSet.getActualDurationSeconds())
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
}
