package com.lukeroche.fit.services.impl;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.services.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public List<ExerciseEntity> findAll() {
        return StreamSupport.stream(exerciseRepository
                                .findAll()
                                .spliterator(),
                        false)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ExerciseEntity> findAll(Pageable pageable) {
        return exerciseRepository.findAll(pageable);
    }

    @Override
    public Optional<ExerciseEntity> findOne(Long id) {
        return exerciseRepository.findById(id);
    }

    @Override
    public boolean isExists(Long id) {
        return exerciseRepository.existsById(id);
    }

    @Override
    public ExerciseEntity partialUpdate(Long id, ExerciseEntity exerciseEntity) {
        exerciseEntity.setId(id);

        return exerciseRepository.findById(id).map(existingExercise -> {
            Optional.ofNullable(exerciseEntity.getName()).ifPresent((existingExercise::setName));
            Optional.ofNullable(exerciseEntity.getDescription()).ifPresent((existingExercise::setDescription));
            return exerciseRepository.save(existingExercise);
        }).orElseThrow(() -> new RuntimeException("Author does not exist"));
    }

    @Override
    public void delete(Long id) {
        exerciseRepository.deleteById(id);
    }
}
