package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.StartSessionRequest;
import com.lukeroche.fit.domain.entities.FriendshipEntity;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkoutLogServiceTests extends AbstractServiceTests {

    @Autowired
    WorkoutLogService workoutLogService;

    @Autowired
    FriendshipService friendshipService;

    @Test
    void startSessionClonesTheWorkoutAndSeedsActualAndTargetFromThePlan() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);

        WorkoutLogEntity log = workoutLogService.startSession(
                workout.getId(), owner.getId(), StartSessionRequest.builder().name("Today").build());

        assertEquals("Today", log.getName());
        assertEquals(workout.getId(), log.getSourceWorkoutId());
        assertEquals(1, log.getLoggedExercises().size());
        LoggedSetEntity set = log.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(8, set.getActualReps());
        assertEquals(8, set.getTargetReps());
        assertEquals(80f, set.getActualWeight());
        assertEquals(80f, set.getTargetWeight());
    }

    @Test
    void secondStartIsRejectedUntilTheOpenSessionIsFinished() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        workoutLogService.startSession(workout.getId(), owner.getId(), null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> workoutLogService.startSession(workout.getId(), owner.getId(), null));
        assertEquals("You already have a session in progress", ex.getMessage());

        WorkoutLogEntity open = workoutLogService.findInProgressForUser(owner.getId()).orElseThrow();
        workoutLogService.finishSession(open.getId(), owner.getId());

        WorkoutLogEntity second = workoutLogService.startSession(workout.getId(), owner.getId(), null);
        LoggedSetEntity seeded = second.getLoggedExercises().getFirst().getLoggedSets().getFirst();
        assertEquals(9, seeded.getActualReps());
        assertEquals(9, seeded.getTargetReps());
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
        assertEquals(8, updated.getTargetReps());
        assertEquals(80f, updated.getTargetWeight());
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
}
