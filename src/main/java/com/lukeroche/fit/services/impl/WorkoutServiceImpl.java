package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.UpdateWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
import com.lukeroche.fit.repositories.WorkoutRepository;
import com.lukeroche.fit.services.WorkoutService;
import jakarta.transaction.Transactional;
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

    public WorkoutServiceImpl(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository, WorkoutExerciseRepository workoutExerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.exerciseRepository = exerciseRepository;
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
        }).orElseThrow(() -> new RuntimeException("Workout does not exist"));
    }

    @Override
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }


    @Override
    public WorkoutExerciseEntity addWorkoutExercise(Long workoutId, AddWorkoutExerciseRequest request){

        WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow();

        ExerciseEntity exercise = exerciseRepository.findById(request.getExerciseId()).orElseThrow();

        WorkoutExerciseEntity workoutExercise = WorkoutExerciseEntity.builder()
                .workoutEntity(workout)
                .exerciseEntity(exercise)
                .orderIndex(workoutExerciseRepository.countByWorkoutEntity_Id(workoutId) + 1)
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
}
