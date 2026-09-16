/*
 * Filename: PlanController.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.controllers;

import com.lukeroche.fit.NotFound;
import com.lukeroche.fit.domain.dto.plan.*;
import com.lukeroche.fit.domain.entities.PlanDayEntity;
import com.lukeroche.fit.domain.entities.PlanEntity;
import com.lukeroche.fit.mappers.PlanDayMapper;
import com.lukeroche.fit.mappers.PlanMapper;
import com.lukeroche.fit.services.PlanService;
import com.lukeroche.fit.services.WorkoutService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * HTTP for weekly plans. At most one plan is active; a new plan stays inactive
 * until {@link #activatePlan}. Unowned ids return 404.
 */
@RestController
public class PlanController {

    private PlanService planService;

    private WorkoutService workoutService;

    private PlanMapper planMapper;

    private PlanDayMapper planDayMapper;

    public PlanController(PlanService planService, WorkoutService workoutService, PlanMapper planMapper, PlanDayMapper planDayMapper) {
        this.planService = planService;
        this.workoutService = workoutService;
        this.planMapper = planMapper;
        this.planDayMapper = planDayMapper;
    }

    /** Always created inactive so it cannot displace the current plan by accident. */
    @PostMapping(path = "/plans")
    public ResponseEntity<PlanResponse> createPlan(@RequestBody PlanRequest planRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        PlanEntity planEntity = planMapper.fromRequest(planRequest);
        planEntity.setCreatedByUserId(userId);
        planEntity.setActive(false);
        PlanEntity saved = planService.save(planEntity);
        return new ResponseEntity<>(planMapper.toResponse(saved), HttpStatus.CREATED);
    }

    /** {@code excludeActive} is for the switch-plan picker so the current plan is omitted. */
    @GetMapping(path = "/plans")
    public Page<PlanResponse> listPlans(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "false") boolean excludeActive,
            Pageable pageable,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<PlanEntity> plans = planService.findAllForUser(userId, query, excludeActive, pageable);
        return plans.map(planMapper::toResponse);
    }

    @GetMapping(path = "/plans/{id}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        PlanEntity plan = planService.findOneForUser(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        return new ResponseEntity<>(planMapper.toResponse(plan), HttpStatus.OK);
    }

    @PatchMapping(path = "/plans/{id}")
    public ResponseEntity<PlanResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody PlanRequest planRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        PlanEntity planEntity = planMapper.fromRequest(planRequest);
        PlanEntity updated = planService.partialUpdate(id, userId, planEntity);
        return new ResponseEntity<>(planMapper.toResponse(updated), HttpStatus.OK);
    }

    @DeleteMapping(path = "/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        planService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/plans/{id}/days/{dayOfWeek}")
    public ResponseEntity<PlanDayResponse> setPlanDay(
            @PathVariable("id") Long id,
            @PathVariable("dayOfWeek") Integer dayOfWeek,
            @RequestBody SetPlanDayRequest setPlanDayRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        NotFound.unless(workoutService.isOwnedByUser(setPlanDayRequest.getWorkoutId(), userId));
        PlanDayEntity saved = planService.setPlanDay(id, userId, dayOfWeek, setPlanDayRequest.getWorkoutId());
        return new ResponseEntity<>(planDayMapper.toResponse(saved), HttpStatus.OK);
    }

    @DeleteMapping(path = "/plans/{id}/days/{dayOfWeek}")
    public ResponseEntity<Void> removePlanDay(
            @PathVariable("id") Long id,
            @PathVariable("dayOfWeek") Integer dayOfWeek,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        planService.removePlanDay(id, dayOfWeek);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /** Turns off any other active plan and sets {@code startDate} to today. */
    @PostMapping(path = "/plans/{id}/activate")
    public ResponseEntity<PlanResponse> activatePlan(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        PlanEntity activated = planService.activate(id, userId);
        return new ResponseEntity<>(planMapper.toResponse(activated), HttpStatus.OK);
    }

    @PostMapping(path = "/plans/{id}/deactivate")
    public ResponseEntity<PlanResponse> deactivatePlan(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(planService.isOwnedByUser(id, userId));
        PlanEntity deactivated = planService.deactivate(id, userId);
        return new ResponseEntity<>(planMapper.toResponse(deactivated), HttpStatus.OK);
    }

    @GetMapping(path = "/plans/active")
    public ResponseEntity<PlanResponse> getActivePlan(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return planService.getActiveForUser(userId)
                .map(planEntity -> new ResponseEntity<>(planMapper.toResponse(planEntity), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Projects the active plan's days forward from today. */
    @GetMapping(path = "/plans/active/upcoming")
    public ResponseEntity<java.util.List<UpcomingWorkoutResponse>> getUpcoming(
            @RequestParam(name = "weeks", defaultValue = "4") int weeks,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return new ResponseEntity<>(planService.getUpcoming(userId, LocalDate.now(), weeks), HttpStatus.OK);
    }

    @GetMapping(path = "/plans/active/next")
    public ResponseEntity<UpcomingWorkoutResponse> getNext(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return planService.getNext(userId, LocalDate.now())
                .map(next -> new ResponseEntity<>(next, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
