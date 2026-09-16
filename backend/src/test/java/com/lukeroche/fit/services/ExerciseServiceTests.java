/*
 * Filename: ExerciseServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LimbPattern;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExerciseServiceTests extends AbstractServiceTests {

    private static final String IN_USE = "This exercise is used in a workout or history and can't be deleted.";

    @Autowired
    WorkoutLogService workoutLogService;

    @Autowired
    WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void saveAppliesTrackingAndDumbbellDefaultsWhenUnset() {
        User owner = newUser("ow");
        ExerciseEntity exercise = ExerciseEntity.builder()
                .name("Curl")
                .description("")
                .createdByUserId(owner.getId())
                .loadingType(LoadingType.DUMBBELL)
                .loadStep(2.0)
                .build();
        exercise.setTracksWeight(null);
        exercise.setTracksDuration(null);
        exercise.setTracksDistance(null);
        exercise.setLimbPattern(null);
        exercise.setIndependentLoads(null);

        ExerciseEntity saved = exerciseService.save(exercise);

        assertTrue(saved.getTracksWeight());
        assertEquals(false, saved.getTracksDuration());
        assertEquals(false, saved.getTracksDistance());
        assertEquals(LimbPattern.BILATERAL, saved.getLimbPattern());
        assertTrue(saved.getIndependentLoads());
    }

    @Test
    void unusedExerciseCanBeDeletedButOneOnAWorkoutCannot() {
        User owner = newUser("ow");
        ExerciseEntity unused = exerciseService.save(ExerciseEntity.builder()
                .name("Spare")
                .description("")
                .createdByUserId(owner.getId())
                .loadingType(LoadingType.BARBELL)
                .loadStep(2.5)
                .tracksWeight(true)
                .build());

        exerciseService.delete(unused.getId());
        assertTrue(exerciseService.findOneForUser(unused.getId(), owner.getId()).isEmpty());

        workoutWithPlannedSet(owner, 8, 80f);
        ExerciseEntity used = benchOf(owner);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> exerciseService.delete(used.getId()));
        assertEquals(IN_USE, ex.getMessage());
        assertTrue(exerciseService.findOneForUser(used.getId(), owner.getId()).isPresent());
    }

    @Test
    void exerciseInSessionHistoryCannotBeDeletedAfterItLeavesTheWorkout() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        ExerciseEntity used = benchOf(owner);
        Long slotId = workoutExerciseRepository
                .findByWorkoutEntity_IdOrderByOrderIndexAsc(workout.getId())
                .getFirst()
                .getId();

        WorkoutLogEntity log = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        workoutLogService.finishSession(log.getId(), owner.getId());
        entityManager.flush();
        entityManager.clear();
        workoutService.removeExerciseFromWorkout(workout.getId(), slotId);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> exerciseService.delete(used.getId()));
        assertEquals(IN_USE, ex.getMessage());
    }

    private ExerciseEntity benchOf(User owner) {
        return exerciseService.findAllForUser(owner.getId(), "Bench", PageRequest.of(0, 5))
                .getContent()
                .getFirst();
    }
}
