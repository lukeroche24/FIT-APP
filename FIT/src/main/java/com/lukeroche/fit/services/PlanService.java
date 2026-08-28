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

public interface PlanService {

    PlanEntity save(PlanEntity planEntity);

    Page<PlanEntity> findAllForUser(UUID userId, String query, boolean excludeActive, Pageable pageable);

    Optional<PlanEntity> findOneForUser(Long id, UUID userId);

    boolean isOwnedByUser(Long id, UUID userId);

    PlanEntity partialUpdate(Long id, UUID userId, PlanEntity planEntity);

    void delete(Long id);

    PlanDayEntity setPlanDay(Long planId, UUID userId, Integer dayOfWeek, Long workoutId);

    void removePlanDay(Long planId, Integer dayOfWeek);

    PlanEntity activate(Long planId, UUID userId);

    PlanEntity deactivate(Long planId, UUID userId);

    Optional<PlanEntity> getActiveForUser(UUID userId);

    List<UpcomingWorkoutResponse> getUpcoming(UUID userId, LocalDate fromDate, int weeksAhead);

    Optional<UpcomingWorkoutResponse> getNext(UUID userId, LocalDate fromDate);
}
