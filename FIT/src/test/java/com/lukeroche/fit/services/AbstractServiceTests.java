package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Shared H2 Spring context and fixtures for service tests.
 */
@SpringBootTest
@Transactional
public abstract class AbstractServiceTests {

    @Autowired
    UserService userService;

    @Autowired
    ExerciseService exerciseService;

    @Autowired
    WorkoutService workoutService;

    protected User newUser(String prefix) {
        String tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return userService.createUser("Test", prefix + tag, prefix + tag + "@ex.com", "password");
    }

    protected WorkoutEntity workoutWithPlannedSet(User owner, int targetReps, float targetWeight) {
        ExerciseEntity exercise = exerciseService.save(ExerciseEntity.builder()
                .name("Bench")
                .description("")
                .createdByUserId(owner.getId())
                .loadingType(LoadingType.BARBELL)
                .loadStep(2.5)
                .tracksWeight(true)
                .build());

        WorkoutEntity workout = workoutService.save(WorkoutEntity.builder()
                .name("Push")
                .description("")
                .createdByUserId(owner.getId())
                .build());

        WorkoutExerciseEntity slot = workoutService.addWorkoutExercise(
                workout.getId(),
                owner.getId(),
                AddWorkoutExerciseRequest.builder()
                        .exerciseId(exercise.getId())
                        .minReps(6)
                        .maxReps(12)
                        .build());

        workoutService.addPlannedSet(
                slot.getId(),
                PlannedSetRequest.builder()
                        .targetReps(targetReps)
                        .targetWeight(targetWeight)
                        .build());

        return workout;
    }
}
