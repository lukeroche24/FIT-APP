package com.lukeroche.fit.services;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Private exercise library for one user. Tracking, loading type, and
 * laterality defaults are applied on create and update.
 */
public interface ExerciseService {
    ExerciseEntity save(ExerciseEntity exercise);

    Page<ExerciseEntity> findAllForUser(UUID userId, String query, Pageable pageable);

    Optional<ExerciseEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    ExerciseEntity partialUpdate(Long id, UUID userId, ExerciseEntity exerciseEntity);

    /**
     * Refuses if the exercise is on a workout or in session history so those
     * rows keep a valid exercise.
     */
    void delete(Long id);
}
