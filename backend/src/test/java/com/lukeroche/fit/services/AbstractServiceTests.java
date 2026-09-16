/*
 * Filename: AbstractServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
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

    @Autowired
    WorkoutExerciseRepository workoutExerciseRepository;

    protected User newUser(String prefix) {
        String tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return userService.createUser("Test", prefix + tag, prefix + tag + "@ex.com", "password");
    }

        @SuppressWarnings("unused")
        protected WorkoutEntity workoutWithPlannedSet(User owner, int targetReps, float targetWeight) {
        return workoutWithPlannedSets(owner, 1, 6, 12);
    }

    protected WorkoutEntity workoutWithPlannedSets(User owner, int setCount, int minReps, int maxReps) {
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
                        .minReps(minReps)
                        .maxReps(maxReps)
                        .build());

        for (int i = 0; i < setCount; i++) {
            workoutService.addPlannedSet(slot.getId(), PlannedSetRequest.builder().build());
        }

        return workout;
    }

    protected WorkoutExerciseEntity firstSlot(WorkoutEntity workout) {
        return workoutExerciseRepository.findByWorkoutEntity_IdOrderByOrderIndexAsc(workout.getId()).getFirst();
    }
}
