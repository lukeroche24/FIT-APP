/*
 * Filename: WorkoutServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkoutServiceTests extends AbstractServiceTests {

    @Autowired
    WorkoutLogService workoutLogService;

    @Test
    void copyKeepsTheSourceRepRangeAndSetCountWithoutCopyingLoad() {
        User owner = newUser("ow");
        WorkoutEntity source = workoutWithPlannedSet(owner, 8, 80f);
        WorkoutLogEntity log = workoutLogService.startSession(source.getId(), owner.getId(), null);
        LoggedSetEntity set = log.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        workoutLogService.updateLoggedSet(set.getId(), LoggedSetRequest.builder()
                .actualReps(5)
                .actualWeight(70f)
                .build());
        workoutLogService.finishSession(log.getId(), owner.getId());

        WorkoutEntity copy = workoutService.copyWorkoutLogToLibrary(log.getId(), owner.getId());

        assertNotEquals(source.getId(), copy.getId());
        assertEquals(owner.getId(), copy.getCreatedByUserId());
        assertEquals("Push(2)", copy.getName());
        assertEquals("Copied from Push", copy.getDescription());

        WorkoutExerciseEntity slot = copy.getWorkoutExercises().getFirst();
        assertEquals(6, slot.getMinReps());
        assertEquals(12, slot.getMaxReps());
        PlannedSetEntity planned = slot.getPlannedSets().getFirst();
        assertNull(planned.getTargetReps());
        assertNull(planned.getTargetWeight());
        assertEquals(benchOf(owner).getId(), slot.getExerciseEntity().getId());
    }

    @Test
    void copyCreatesALibraryExerciseForAFriendThenReusesItOnTheNextCopy() {
        User owner = newUser("ow");
        User friend = newUser("fr");
        WorkoutEntity source = workoutWithPlannedSet(owner, 8, 80f);
        Long ownerExerciseId = benchOf(owner).getId();
        WorkoutLogEntity log = finishedSession(owner, source);

        WorkoutEntity firstCopy = workoutService.copyWorkoutLogToLibrary(log.getId(), friend.getId());
        ExerciseEntity created = firstCopy.getWorkoutExercises().getFirst().getExerciseEntity();
        assertNotEquals(ownerExerciseId, created.getId());
        assertEquals(friend.getId(), created.getCreatedByUserId());
        assertEquals("Bench", created.getName());
        assertEquals("Push", firstCopy.getName());

        WorkoutEntity secondCopy = workoutService.copyWorkoutLogToLibrary(log.getId(), friend.getId());
        assertEquals("Push(2)", secondCopy.getName());
        assertEquals(created.getId(), secondCopy.getWorkoutExercises().getFirst().getExerciseEntity().getId());
    }

    private WorkoutLogEntity finishedSession(User owner, WorkoutEntity source) {
        WorkoutLogEntity log = workoutLogService.startSession(source.getId(), owner.getId(), null);
        return workoutLogService.finishSession(log.getId(), owner.getId());
    }

    private ExerciseEntity benchOf(User owner) {
        return exerciseService.findAllForUser(owner.getId(), "Bench", PageRequest.of(0, 5))
                .getContent()
                .getFirst();
    }
}
