package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.plan.UpcomingWorkoutResponse;
import com.lukeroche.fit.domain.entities.PlanDayEntity;
import com.lukeroche.fit.domain.entities.PlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Weekly plans. At most one plan is {@code active}; activating another
 * turns the current one off and sets {@code startDate} to today.
 */
public interface PlanService {

    PlanEntity save(PlanEntity planEntity);

    Page<PlanEntity> findAllForUser(UUID userId, String query, boolean excludeActive, Pageable pageable);

    Optional<PlanEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    PlanEntity partialUpdate(Long id, UUID userId, PlanEntity planEntity);

    void delete(Long id);

    /** Assigns a workout to a weekday slot, replacing any workout already there. */
    PlanDayEntity setPlanDay(Long planId, UUID userId, Integer dayOfWeek, Long workoutId);

    void removePlanDay(Long planId, Integer dayOfWeek);

    /**
     * Makes this the only active plan. The previous active plan is turned off
     * and this plan's {@code startDate} is set to today.
     */
    PlanEntity activate(Long planId, UUID userId);

    PlanEntity deactivate(Long planId, UUID userId);

    Optional<PlanEntity> getActiveForUser(UUID userId);

    /**
     * One entry per calendar day from {@code startDate} through the plan length.
     * Days without a workout are {@code REST}. A completed log for that date
     * and source workout is {@code COMPLETED}; past unset days are {@code MISSED}.
     */
    List<UpcomingWorkoutResponse> getUpcoming(UUID userId, LocalDate fromDate, int weeksAhead);

    /**
     * First scheduled workout on or after {@code fromDate} that is not yet
     * completed. Empty when nothing is due.
     */
    Optional<UpcomingWorkoutResponse> getNext(UUID userId, LocalDate fromDate);
}
