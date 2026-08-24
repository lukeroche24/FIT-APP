package com.lukeroche.fit.services;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ExerciseService {
    ExerciseEntity save(ExerciseEntity exercise);

    Page<ExerciseEntity> findAllForUser(UUID userId, Pageable pageable);

    Optional<ExerciseEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    ExerciseEntity partialUpdate(Long id, UUID userId, ExerciseEntity exerciseEntity);

    void delete(Long id);
}
