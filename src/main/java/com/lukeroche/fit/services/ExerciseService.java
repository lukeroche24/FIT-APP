package com.lukeroche.fit.services;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

public interface ExerciseService {
    ExerciseEntity save(ExerciseEntity exercise);

    List<ExerciseEntity> findAll();

    Page<ExerciseEntity> findAll(Pageable pageable);

    Optional<ExerciseEntity> findOne(Long id);

    boolean isExists(Long id);

    ExerciseEntity partialUpdate(Long id, ExerciseEntity exerciseEntity);

    void delete(Long id);
}
