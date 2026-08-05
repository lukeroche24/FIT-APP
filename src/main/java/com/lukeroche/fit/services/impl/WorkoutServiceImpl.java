package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
import com.lukeroche.fit.repositories.WorkoutRepository;
import com.lukeroche.fit.services.WorkoutService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class WorkoutServiceImpl implements WorkoutService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private WorkoutRepository workoutRepository;

    private ExerciseRepository exerciseRepository;

    public WorkoutServiceImpl(WorkoutRepository workoutRepository, WorkoutExerciseRepository workoutExerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
    }

    @Override
    public WorkoutEntity save(WorkoutEntity workoutEntity) {
        return workoutRepository.save(workoutEntity);
    }

    @Override
    public List<WorkoutEntity> findAll() {
        return StreamSupport.stream(workoutRepository
                                .findAll()
                                .spliterator(),
                        false)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WorkoutEntity> findAll(Pageable pageable) {
        return workoutRepository.findAll(pageable);
    }

    @Override
    public Optional<WorkoutEntity> findOne(Long id) {
        return workoutRepository.findById(id);
    }

    @Override
    public boolean isExists(Long id) {
        return workoutRepository.existsById(id);
    }

    @Override
    public WorkoutEntity partialUpdate(Long id, WorkoutEntity workoutEntity) {
        workoutEntity.setId(id);

        return workoutRepository.findById(id).map(existingWorkout -> {
            Optional.ofNullable(workoutEntity.getName()).ifPresent((existingWorkout::setName));
            Optional.ofNullable(workoutEntity.getDescription()).ifPresent((existingWorkout::setDescription));
            Optional.ofNullable(workoutEntity.getVisibility()).ifPresent((existingWorkout::setVisibility));
            return workoutRepository.save(existingWorkout);
        }).orElseThrow(() -> new RuntimeException("Author does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }


    @Override
    public WorkoutExerciseEntity addExercise(Long workoutId, AddWorkoutExerciseRequest request){

        WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow();

        ExerciseEntity exercise = exerciseRepository.findById(request.getExerciseId()).orElseThrow();

        WorkoutExerciseEntity workoutExercise = WorkoutExerciseEntity.builder()
                .workoutEntity(workout)
                .exerciseEntity(exercise)
                .orderIndex(request.getOrderIndex())
                .build();

        return workoutExerciseRepository.save(workoutExercise);
    }


}
