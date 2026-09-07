package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.StartSessionRequest;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.repositories.WorkoutLogRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkoutLogServiceTests extends AbstractServiceTests {

    @Autowired
    WorkoutLogService workoutLogService;

    @Autowired
    FriendshipService friendshipService;

    @Autowired
    WorkoutLogRepository workoutLogRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void startSessionClonesTheWorkoutAndSeedsMinRepsWithEmptyWeight() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);

        WorkoutLogEntity log = workoutLogService.startSession(
                workout.getId(), owner.getId(), StartSessionRequest.builder().name("Today").build());

        assertEquals("Today", log.getName());
        assertEquals(workout.getId(), log.getSourceWorkoutId());
        assertEquals(1, log.getLoggedExercises().size());
        LoggedSetEntity set = log.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(6, set.getActualReps());
        assertEquals(6, set.getTargetReps());
        assertNull(set.getActualWeight());
        assertNull(set.getTargetWeight());
    }

    @Test
    void secondStartIsRejectedUntilTheOpenSessionIsFinished() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        WorkoutLogEntity open = workoutLogService.startSession(workout.getId(), owner.getId(), null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> workoutLogService.startSession(workout.getId(), owner.getId(), null));
        assertEquals("You already have a session in progress", ex.getMessage());

        LoggedSetEntity firstSet = open.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        workoutLogService.updateLoggedSet(firstSet.getId(), LoggedSetRequest.builder()
                .actualReps(6)
                .actualWeight(80f)
                .build());
        workoutLogService.finishSession(open.getId(), owner.getId());

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity seeded = second.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(7, seeded.getActualReps());
        assertEquals(7, seeded.getTargetReps());
        assertEquals(80f, seeded.getActualWeight());
    }

    @Test
    void failOnTheTargetRepDoesNotProgress() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSets(owner, 3, 8, 8);
        WorkoutLogEntity first = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        List<LoggedSetEntity> firstSets = first.getLoggedExercises().getFirst().getLoggedSets();
        logSet(firstSets.get(0).getId(), 8, 50f);
        logSet(firstSets.get(1).getId(), 8, 50f);
        logSet(firstSets.get(2).getId(), 8, 50f, true);
        workoutLogService.finishSession(first.getId(), owner.getId());

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity seeded = second.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(8, seeded.getActualReps());
        assertEquals(8, seeded.getTargetReps());
        assertEquals(50f, seeded.getActualWeight());
    }

    @Test
    void failOnABonusRepStillProgresses() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSets(owner, 1, 6, 12);
        WorkoutLogEntity first = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        logSet(first.getLoggedExercises().getFirst().getLoggedSets().getFirst().getId(), 9, 80f, true);
        workoutLogService.finishSession(first.getId(), owner.getId());

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity seeded = second.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(10, seeded.getActualReps());
        assertEquals(10, seeded.getTargetReps());
        assertEquals(80f, seeded.getActualWeight());
    }

    @Test
    void updateLoggedSetChangesActualsButNotTheSeededTarget() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        WorkoutLogEntity log = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity set = log.getLoggedExercises().getFirst().getLoggedSets().getFirst();

        LoggedSetEntity updated = workoutLogService.updateLoggedSet(
                set.getId(),
                LoggedSetRequest.builder()
                        .actualReps(5)
                        .actualWeight(70f)
                        .failed(true)
                        .build());

        assertEquals(5, updated.getActualReps());
        assertEquals(70f, updated.getActualWeight());
        assertTrue(updated.getFailed());
        assertEquals(6, updated.getTargetReps());
        assertNull(updated.getTargetWeight());
    }

    @Test
    void missWithin14DaysReplaysActualsAndLeavesExtraSetsEmpty() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSets(owner, 3, 8, 8);
        WorkoutLogEntity first = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        List<LoggedSetEntity> firstSets = first.getLoggedExercises().getFirst().getLoggedSets();
        logSet(firstSets.get(0).getId(), 8, 50f);
        logSet(firstSets.get(1).getId(), 8, 50f);
        logSet(firstSets.get(2).getId(), 6, 50f);
        workoutLogService.finishSession(first.getId(), owner.getId());

        workoutService.addPlannedSet(firstSlot(workout).getId(), PlannedSetRequest.builder().build());

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        List<LoggedSetEntity> seeded = second.getLoggedExercises().getFirst().getLoggedSets();
        assertEquals(4, seeded.size());
        assertEquals(8, seeded.get(0).getActualReps());
        assertEquals(8, seeded.get(1).getActualReps());
        assertEquals(6, seeded.get(2).getActualReps());
        assertNull(seeded.get(3).getActualReps());
        assertNull(seeded.get(3).getActualWeight());
        for (LoggedSetEntity set : seeded) {
            assertEquals(8, set.getTargetReps());
        }
        assertEquals(50f, seeded.get(0).getActualWeight());
        assertEquals(50f, seeded.get(1).getActualWeight());
        assertEquals(50f, seeded.get(2).getActualWeight());
    }

    @Test
    void missOlderThan14DaysDoesNotReplayActuals() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSets(owner, 3, 8, 8);
        WorkoutLogEntity first = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        List<LoggedSetEntity> firstSets = first.getLoggedExercises().getFirst().getLoggedSets();
        logSet(firstSets.get(0).getId(), 8, 50f);
        logSet(firstSets.get(1).getId(), 8, 50f);
        logSet(firstSets.get(2).getId(), 6, 50f);
        workoutLogService.finishSession(first.getId(), owner.getId());
        first.setCompletedAt(LocalDateTime.now().minusDays(15));
        workoutLogRepository.save(first);

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        List<LoggedSetEntity> seeded = second.getLoggedExercises().getFirst().getLoggedSets();
        assertEquals(8, seeded.get(0).getActualReps());
        assertEquals(8, seeded.get(1).getActualReps());
        assertEquals(8, seeded.get(2).getActualReps());
        assertEquals(50f, seeded.get(0).getActualWeight());
    }

    @Test
    void replaySkipsExercisesRemovedFromTheWorkout() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSets(owner, 1, 8, 8);
        ExerciseEntity squat = exerciseService.save(ExerciseEntity.builder()
                .name("Squat")
                .description("")
                .createdByUserId(owner.getId())
                .loadingType(LoadingType.BARBELL)
                .loadStep(2.5)
                .tracksWeight(true)
                .build());
        WorkoutExerciseEntity squatSlot = workoutService.addWorkoutExercise(
                workout.getId(),
                owner.getId(),
                AddWorkoutExerciseRequest.builder()
                        .exerciseId(squat.getId())
                        .minReps(8)
                        .maxReps(8)
                        .build());
        workoutService.addPlannedSet(squatSlot.getId(), PlannedSetRequest.builder().build());

        WorkoutLogEntity first = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity benchSet = first.getLoggedExercises().stream()
                .filter(exercise -> "Bench".equals(exercise.getExerciseEntity().getName()))
                .findFirst()
                .orElseThrow()
                .getLoggedSets()
                .getFirst();
        LoggedSetEntity squatSet = first.getLoggedExercises().stream()
                .filter(exercise -> "Squat".equals(exercise.getExerciseEntity().getName()))
                .findFirst()
                .orElseThrow()
                .getLoggedSets()
                .getFirst();
        logSet(benchSet.getId(), 6, 50f);
        logSet(squatSet.getId(), 8, 100f);
        workoutLogService.finishSession(first.getId(), owner.getId());

        Long squatSlotId = squatSlot.getId();
        entityManager.flush();
        entityManager.clear();
        workoutService.removeExerciseFromWorkout(workout.getId(), squatSlotId);

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        assertEquals(1, second.getLoggedExercises().size());
        assertEquals("Bench", second.getLoggedExercises().getFirst().getExerciseEntity().getName());
        LoggedSetEntity seeded = second.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(6, seeded.getActualReps());
        assertEquals(50f, seeded.getActualWeight());
        assertEquals(8, seeded.getTargetReps());
    }

    @Test
    void ownerSeesInProgressLogsButFriendsDoNotUntilFinished() {
        User owner = newUser("ow");
        User friend = newUser("fr");
        User stranger = newUser("st");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        WorkoutLogEntity log = workoutLogService.startSession(workout.getId(), owner.getId(), null);

        assertTrue(workoutLogService.findVisibleToUser(log.getId(), owner.getId()).isPresent());
        assertTrue(workoutLogService.findVisibleToUser(log.getId(), friend.getId()).isEmpty());
        assertFalse(workoutLogService.canCopy(log.getId(), owner.getId()));

        FriendshipEntity request = friendshipService.sendRequest(friend.getId(), owner.getUsername());
        friendshipService.acceptRequest(request.getId());

        assertTrue(workoutLogService.findVisibleToUser(log.getId(), friend.getId()).isEmpty());

        workoutLogService.finishSession(log.getId(), owner.getId());

        assertTrue(workoutLogService.findVisibleToUser(log.getId(), friend.getId()).isPresent());
        assertTrue(workoutLogService.findVisibleToUser(log.getId(), stranger.getId()).isEmpty());
        assertTrue(workoutLogService.canCopy(log.getId(), friend.getId()));
        assertFalse(workoutLogService.canCopy(log.getId(), stranger.getId()));
        assertTrue(workoutLogService.canCopy(log.getId(), owner.getId()));
    }

    private void logSet(Long setId, int reps, float weight) {
        logSet(setId, reps, weight, false);
    }

    private void logSet(Long setId, int reps, float weight, boolean failed) {
        workoutLogService.updateLoggedSet(setId, LoggedSetRequest.builder()
                .actualReps(reps)
                .actualWeight(weight)
                .failed(failed)
                .build());
    }
}
