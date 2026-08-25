package com.lukeroche.fit.services.impl;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.repositories.LoggedExerciseRepository;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
import com.lukeroche.fit.services.ExerciseService;
import com.lukeroche.fit.services.progression.LoadingTypeSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private ExerciseRepository exerciseRepository;
    private WorkoutExerciseRepository workoutExerciseRepository;
    private LoggedExerciseRepository loggedExerciseRepository;
    private LoadingTypeSuggestion loadingTypeSuggestion;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            LoggedExerciseRepository loggedExerciseRepository,
            LoadingTypeSuggestion loadingTypeSuggestion) {
        this.exerciseRepository = exerciseRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.loggedExerciseRepository = loggedExerciseRepository;
        this.loadingTypeSuggestion = loadingTypeSuggestion;
    }

    @Override
    public ExerciseEntity save(ExerciseEntity exerciseEntity) {
        loadingTypeSuggestion.applyDefaults(exerciseEntity);
        return exerciseRepository.save(exerciseEntity);
    }

    @Override
    public Page<ExerciseEntity> findAllForUser(UUID userId, Pageable pageable) {
        return exerciseRepository.findByCreatedByUserId(userId, pageable);
    }

    @Override
    public Optional<ExerciseEntity> findOneForUser(Long id, UUID userId) {
        return exerciseRepository.findByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public boolean isOwnedByUser(Long id, UUID userId) {
        return exerciseRepository.existsByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public ExerciseEntity partialUpdate(Long id, UUID userId, ExerciseEntity exerciseEntity) {
        exerciseEntity.setId(id);

        return exerciseRepository.findByIdAndCreatedByUserId(id, userId).map(existingExercise -> {
            Optional.ofNullable(exerciseEntity.getName()).ifPresent((existingExercise::setName));
            Optional.ofNullable(exerciseEntity.getDescription()).ifPresent((existingExercise::setDescription));
            Optional.ofNullable(exerciseEntity.getLoadStep()).ifPresent((existingExercise::setLoadStep));
            Optional.ofNullable(exerciseEntity.getLoadingType()).ifPresent((existingExercise::setLoadingType));
            loadingTypeSuggestion.applyDefaults(existingExercise);
            return exerciseRepository.save(existingExercise);
        }).orElseThrow(() -> new RuntimeException("Exercise does not exist"));
    }

    @Override
    public void delete(Long id) {
        if (workoutExerciseRepository.existsByExerciseEntity_Id(id)
                || loggedExerciseRepository.existsByExerciseEntity_Id(id)) {
            throw new IllegalStateException(
                    "This exercise is used in a workout or history and can't be deleted.");
        }
        exerciseRepository.deleteById(id);
    }
}
