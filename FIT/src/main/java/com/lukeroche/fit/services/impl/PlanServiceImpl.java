package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.plan.PlanOccurrenceStatus;
import com.lukeroche.fit.domain.dto.plan.UpcomingWorkoutResponse;
import com.lukeroche.fit.domain.entities.PlanDayEntity;
import com.lukeroche.fit.domain.entities.PlanEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.mappers.WorkoutMapper;
import com.lukeroche.fit.repositories.PlanDayRepository;
import com.lukeroche.fit.repositories.PlanRepository;
import com.lukeroche.fit.repositories.WorkoutLogRepository;
import com.lukeroche.fit.repositories.WorkoutRepository;
import com.lukeroche.fit.services.PlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanDayRepository planDayRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutMapper workoutMapper;

    public PlanServiceImpl(PlanRepository planRepository,
                           PlanDayRepository planDayRepository,
                           WorkoutRepository workoutRepository,
                           WorkoutLogRepository workoutLogRepository,
                           WorkoutMapper workoutMapper) {
        this.planRepository = planRepository;
        this.planDayRepository = planDayRepository;
        this.workoutRepository = workoutRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.workoutMapper = workoutMapper;
    }

    @Override
    public PlanEntity save(PlanEntity planEntity) {
        return planRepository.save(planEntity);
    }

    @Override
    public Page<PlanEntity> findAllForUser(UUID userId, String query, boolean excludeActive, Pageable pageable) {
        boolean hasQuery = query != null && !query.isBlank();
        String term = hasQuery ? query.trim() : "";
        if (excludeActive) {
            if (!hasQuery) {
                return planRepository.findByCreatedByUserIdAndActiveFalse(userId, pageable);
            }
            return planRepository.findByCreatedByUserIdAndActiveFalseAndNameContainingIgnoreCase(userId, term, pageable);
        }
        if (!hasQuery) {
            return planRepository.findByCreatedByUserId(userId, pageable);
        }
        return planRepository.findByCreatedByUserIdAndNameContainingIgnoreCase(userId, term, pageable);
    }

    @Override
    public Optional<PlanEntity> findOneForUser(Long id, UUID userId) {
        return planRepository.findByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public boolean isOwnedByUser(Long id, UUID userId) {
        return planRepository.existsByIdAndCreatedByUserId(id, userId);
    }

    @Override
    public PlanEntity partialUpdate(Long id, UUID userId, PlanEntity planEntity) {
        return planRepository.findByIdAndCreatedByUserId(id, userId).map(existingPlan -> {
            Optional.ofNullable(planEntity.getName()).ifPresent(existingPlan::setName);
            Optional.ofNullable(planEntity.getWeeks()).ifPresent(existingPlan::setWeeks);
            return planRepository.save(existingPlan);
        }).orElseThrow(() -> new RuntimeException("Plan does not exist"));
    }

    @Override
    public void delete(Long id) {
        planRepository.deleteById(id);
    }

    @Override
    public PlanDayEntity setPlanDay(Long planId, UUID userId, Integer dayOfWeek, Long workoutId) {
        PlanEntity plan = planRepository.findByIdAndCreatedByUserId(planId, userId).orElseThrow();
        WorkoutEntity workout = workoutRepository.findByIdAndCreatedByUserId(workoutId, userId).orElseThrow();

        PlanDayEntity planDay = planDayRepository.findByPlanEntity_IdAndDayOfWeek(planId, dayOfWeek)
                .orElseGet(() -> PlanDayEntity.builder()
                        .planEntity(plan)
                        .dayOfWeek(dayOfWeek)
                        .build());
        planDay.setWorkoutEntity(workout);

        return planDayRepository.save(planDay);
    }

    @Override
    public void removePlanDay(Long planId, Integer dayOfWeek) {
        planDayRepository.findByPlanEntity_IdAndDayOfWeek(planId, dayOfWeek)
                .ifPresent(planDayRepository::delete);
    }

    @Override
    public PlanEntity activate(Long planId, UUID userId) {
        planRepository.findByCreatedByUserIdAndActiveTrue(userId)
                .filter(existingActive -> !existingActive.getId().equals(planId))
                .ifPresent(existingActive -> {
                    existingActive.setActive(false);
                    planRepository.save(existingActive);
                });

        PlanEntity plan = planRepository.findByIdAndCreatedByUserId(planId, userId).orElseThrow();
        plan.setActive(true);
        plan.setStartDate(LocalDate.now());
        return planRepository.save(plan);
    }

    @Override
    public PlanEntity deactivate(Long planId, UUID userId) {
        PlanEntity plan = planRepository.findByIdAndCreatedByUserId(planId, userId).orElseThrow();
        plan.setActive(false);
        return planRepository.save(plan);
    }

    @Override
    public Optional<PlanEntity> getActiveForUser(UUID userId) {
        return planRepository.findByCreatedByUserIdAndActiveTrue(userId);
    }

    @Override
    public List<UpcomingWorkoutResponse> getUpcoming(UUID userId, LocalDate today, int weeksAhead) {
        Optional<PlanEntity> activePlan = getActiveForUser(userId);
        if (activePlan.isEmpty()) {
            return List.of();
        }

        PlanEntity plan = activePlan.get();
        LocalDate startDate = plan.getStartDate() != null ? plan.getStartDate() : today;
        int weeks = plan.getWeeks() != null ? plan.getWeeks() : weeksAhead;
        LocalDate planEnd = startDate.plusDays(weeks * 7L);

        Map<Integer, PlanDayEntity> dayMap = new HashMap<>();
        for (PlanDayEntity planDay : planDayRepository.findByPlanEntity_Id(plan.getId())) {
            dayMap.put(planDay.getDayOfWeek(), planDay);
        }

        Map<String, Long> completedByDateAndWorkout = completedLogsByDate(userId, startDate, planEnd);

        List<UpcomingWorkoutResponse> results = new java.util.ArrayList<>();
        for (LocalDate date = startDate; date.isBefore(planEnd); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            int weekNumber = (int) (ChronoUnit.DAYS.between(startDate, date) / 7) + 1;
            PlanDayEntity planDay = dayMap.get(dayOfWeek);
            WorkoutEntity workout = planDay != null ? planDay.getWorkoutEntity() : null;
            Long workoutLogId = null;
            PlanOccurrenceStatus status = PlanOccurrenceStatus.REST;

            if (workout != null) {
                workoutLogId = completedByDateAndWorkout.get(date + ":" + workout.getId());
                if (workoutLogId != null) {
                    status = PlanOccurrenceStatus.COMPLETED;
                } else if (date.isBefore(today)) {
                    status = PlanOccurrenceStatus.MISSED;
                } else if (date.isEqual(today)) {
                    status = PlanOccurrenceStatus.DUE;
                } else {
                    status = PlanOccurrenceStatus.UPCOMING;
                }
            }

            results.add(UpcomingWorkoutResponse.builder()
                    .date(date)
                    .dayOfWeek(dayOfWeek)
                    .weekNumber(weekNumber)
                    .workout(workout != null ? workoutMapper.toResponse(workout) : null)
                    .status(status)
                    .workoutLogId(workoutLogId)
                    .build());
        }

        return results;
    }

    @Override
    public Optional<UpcomingWorkoutResponse> getNext(UUID userId, LocalDate today) {
        return getUpcoming(userId, today, 4).stream()
                .filter(entry -> entry.getWorkout() != null)
                .filter(entry -> !entry.getDate().isBefore(today))
                .filter(entry -> entry.getStatus() != PlanOccurrenceStatus.COMPLETED)
                .findFirst();
    }

    private Map<String, Long> completedLogsByDate(UUID userId, LocalDate startDate, LocalDate planEnd) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = planEnd.atStartOfDay();
        List<WorkoutLogEntity> logs =
                workoutLogRepository.findByCreatedByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        userId, from, to);

        Map<String, Long> completed = new HashMap<>();
        for (WorkoutLogEntity log : logs) {
            if (log.getSourceWorkoutId() == null || log.getCompletedAt() == null) {
                continue;
            }
            String key = log.getCompletedAt().toLocalDate() + ":" + log.getSourceWorkoutId();
            completed.putIfAbsent(key, log.getId());
        }
        return completed;
    }
}
