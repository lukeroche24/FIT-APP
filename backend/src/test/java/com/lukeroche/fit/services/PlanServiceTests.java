/*
 * Filename: PlanServiceTests.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.plan.PlanOccurrenceStatus;
import com.lukeroche.fit.domain.dto.plan.UpcomingWorkoutResponse;
import com.lukeroche.fit.domain.entities.PlanEntity;
import com.lukeroche.fit.domain.entities.User;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.repositories.PlanRepository;
import com.lukeroche.fit.repositories.WorkoutLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanServiceTests extends AbstractServiceTests {

    private static final LocalDate PLAN_START = LocalDate.of(2026, 8, 17);

    @Autowired
    PlanService planService;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    WorkoutLogRepository workoutLogRepository;

    @Test
    void activateTurnsOffThePreviousPlanAndSetsStartDateToToday() {
        User owner = newUser("ow");
        PlanEntity first = planService.save(PlanEntity.builder()
                .createdByUserId(owner.getId())
                .name("A")
                .weeks(4)
                .active(false)
                .build());
        PlanEntity second = planService.save(PlanEntity.builder()
                .createdByUserId(owner.getId())
                .name("B")
                .weeks(4)
                .active(false)
                .build());

        planService.activate(first.getId(), owner.getId());
        planService.activate(second.getId(), owner.getId());

        PlanEntity reloadedFirst = planRepository.findById(first.getId()).orElseThrow();
        PlanEntity reloadedSecond = planRepository.findById(second.getId()).orElseThrow();
        assertFalse(reloadedFirst.getActive());
        assertTrue(reloadedSecond.getActive());
        assertEquals(LocalDate.now(), reloadedSecond.getStartDate());
        assertEquals(second.getId(), planService.getActiveForUser(owner.getId()).orElseThrow().getId());
    }

    @Test
    void upcomingMarksRestMissedDueUpcomingAndCompleted() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        PlanEntity plan = planRepository.save(PlanEntity.builder()
                .createdByUserId(owner.getId())
                .name("Block")
                .weeks(2)
                .active(true)
                .startDate(PLAN_START)
                .build());
        planService.setPlanDay(plan.getId(), owner.getId(), 1, workout.getId());

        List<UpcomingWorkoutResponse> fromMonday = planService.getUpcoming(owner.getId(), PLAN_START, 4);
        assertEquals(14, fromMonday.size());
        UpcomingWorkoutResponse firstMonday = day(fromMonday, PLAN_START);
        UpcomingWorkoutResponse tuesday = day(fromMonday, PLAN_START.plusDays(1));
        UpcomingWorkoutResponse secondMonday = day(fromMonday, PLAN_START.plusWeeks(1));

        assertEquals(PlanOccurrenceStatus.DUE, firstMonday.getStatus());
        assertEquals(PlanOccurrenceStatus.REST, tuesday.getStatus());
        assertEquals(PlanOccurrenceStatus.UPCOMING, secondMonday.getStatus());
        assertEquals(workout.getId(), firstMonday.getWorkout().getId());

        List<UpcomingWorkoutResponse> fromWednesday = planService.getUpcoming(
                owner.getId(), PLAN_START.plusDays(2), 4);
        assertEquals(PlanOccurrenceStatus.MISSED, day(fromWednesday, PLAN_START).getStatus());

        workoutLogRepository.save(WorkoutLogEntity.builder()
                .createdByUserId(owner.getId())
                .sourceWorkoutId(workout.getId())
                .name("Push")
                .startedAt(PLAN_START.atTime(10, 0))
                .completedAt(PLAN_START.atTime(11, 0))
                .build());

        List<UpcomingWorkoutResponse> afterLog = planService.getUpcoming(owner.getId(), PLAN_START, 4);
        UpcomingWorkoutResponse completedMonday = day(afterLog, PLAN_START);
        assertEquals(PlanOccurrenceStatus.COMPLETED, completedMonday.getStatus());
        assertEquals(
                PlanOccurrenceStatus.UPCOMING,
                day(afterLog, PLAN_START.plusWeeks(1)).getStatus());
    }

    @Test
    void getNextIsTheFirstIncompleteScheduledDayFromToday() {
        User owner = newUser("ow");
        WorkoutEntity workout = workoutWithPlannedSet(owner, 8, 80f);
        PlanEntity plan = planRepository.save(PlanEntity.builder()
                .createdByUserId(owner.getId())
                .name("Block")
                .weeks(2)
                .active(true)
                .startDate(PLAN_START)
                .build());
        planService.setPlanDay(plan.getId(), owner.getId(), 1, workout.getId());

        UpcomingWorkoutResponse next = planService.getNext(owner.getId(), PLAN_START).orElseThrow();
        assertEquals(PLAN_START, next.getDate());
        assertEquals(PlanOccurrenceStatus.DUE, next.getStatus());

        workoutLogRepository.save(WorkoutLogEntity.builder()
                .createdByUserId(owner.getId())
                .sourceWorkoutId(workout.getId())
                .name("Push")
                .startedAt(PLAN_START.atTime(10, 0))
                .completedAt(PLAN_START.atTime(11, 0))
                .build());

        UpcomingWorkoutResponse following = planService.getNext(owner.getId(), PLAN_START).orElseThrow();
        assertEquals(PLAN_START.plusWeeks(1), following.getDate());
        assertEquals(PlanOccurrenceStatus.UPCOMING, following.getStatus());
    }

    @Test
    void upcomingIsEmptyWhenNothingIsActive() {
        User owner = newUser("ow");
        assertTrue(planService.getUpcoming(owner.getId(), LocalDate.now(), 4).isEmpty());
        assertTrue(planService.getNext(owner.getId(), LocalDate.now()).isEmpty());
    }

    private static UpcomingWorkoutResponse day(List<UpcomingWorkoutResponse> days, LocalDate date) {
        return days.stream()
                .filter(entry -> entry.getDate().equals(date))
                .findFirst()
                .orElseThrow();
    }
}
