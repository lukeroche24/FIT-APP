package com.lukeroche.fit.services.impl;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.services.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private ExerciseRepository exerciseRepository;

    public ExerciseServiceImpl(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public ExerciseEntity save(ExerciseEntity exerciseEntity) {
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
            return exerciseRepository.save(existingExercise);
        }).orElseThrow(() -> new RuntimeException("Exercise does not exist"));
    }

    @Override
    public void delete(Long id) {
        exerciseRepository.deleteById(id);
    }
}
