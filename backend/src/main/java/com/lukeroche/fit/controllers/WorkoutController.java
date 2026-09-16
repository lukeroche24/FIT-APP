/*
 * Filename: WorkoutController.java
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
import com.lukeroche.fit.domain.dto.workout.*;
import com.lukeroche.fit.domain.dto.workoutlog.*;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.mappers.PlannedSetMapper;
import com.lukeroche.fit.mappers.WorkoutExerciseMapper;
import com.lukeroche.fit.mappers.WorkoutLogMapper;
import com.lukeroche.fit.mappers.WorkoutMapper;
import com.lukeroche.fit.services.ExerciseService;
import com.lukeroche.fit.services.WorkoutLogService;
import com.lukeroche.fit.services.WorkoutService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * HTTP for workout templates. Nested exercises and planned sets are the
 * prescription; starting a session clones that into a log and seeds load
 * from progression. Unowned ids return 404.
 */
@RestController
public class WorkoutController {

    private WorkoutService workoutService;

    private ExerciseService exerciseService;

    private WorkoutMapper workoutMapper;

    private WorkoutExerciseMapper workoutExerciseMapper;

    private PlannedSetMapper plannedSetMapper;

    private WorkoutLogService workoutLogService;

    private WorkoutLogMapper workoutLogMapper;

    public WorkoutController(WorkoutService workoutService, ExerciseService exerciseService, WorkoutMapper workoutMapper, WorkoutExerciseMapper workoutExerciseMapper, PlannedSetMapper plannedSetMapper, WorkoutLogService workoutLogService, WorkoutLogMapper workoutLogMapper) {
        this.workoutService = workoutService;
        this.exerciseService = exerciseService;
        this.workoutMapper = workoutMapper;
        this.workoutExerciseMapper = workoutExerciseMapper;
        this.plannedSetMapper = plannedSetMapper;
        this.workoutLogService = workoutLogService;
        this.workoutLogMapper = workoutLogMapper;
    }


    @PostMapping(path= "/workouts")
    public ResponseEntity<WorkoutResponse> createWorkout(@RequestBody WorkoutRequest workoutRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        workoutEntity.setCreatedByUserId(userId);
        WorkoutEntity savedWorkoutEntity = workoutService.save(workoutEntity);

        return new ResponseEntity<>(workoutMapper.toResponse(savedWorkoutEntity), HttpStatus.CREATED);
    }

    @PostMapping(path = "/workouts/{id}/exercises")
    public ResponseEntity<WorkoutExerciseResponse> addWorkoutExercise(@PathVariable Long id, @RequestBody AddWorkoutExerciseRequest addRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(id, userId));
        NotFound.unless(exerciseService.isOwnedByUser(addRequest.getExerciseId(), userId));

        WorkoutExerciseEntity saved = workoutService.addWorkoutExercise(id, userId, addRequest);
        return new ResponseEntity<>(workoutExerciseMapper.toResponse(saved), HttpStatus.CREATED);
    }


    @GetMapping(path = "/workouts")
    public Page<WorkoutResponse> listWorkouts(
            @RequestParam(required = false) String query,
            Pageable pageable,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<WorkoutEntity> workouts = workoutService.findAllForUser(userId, query, pageable);
        return workouts.map(workoutMapper::toResponse);
    }


    @GetMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        WorkoutEntity workout = workoutService.findOneForUser(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        return new ResponseEntity<>(workoutMapper.toResponse(workout), HttpStatus.OK);
    }

    @PatchMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody WorkoutRequest workoutRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(id, userId));

        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        WorkoutEntity updatedWorkout = workoutService.partialUpdate(id, userId, workoutEntity);
        return new ResponseEntity<>(
                workoutMapper.toResponse(updatedWorkout),
                HttpStatus.OK
        );
    }

    /** PATCH of a nested exercise, including {@code orderIndex}. */
    @PatchMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> updateWorkoutExercise(
            @PathVariable("workoutId") Long workoutId,
            @PathVariable("workoutExerciseId") Long workoutExerciseId,
            @RequestBody UpdateWorkoutExerciseRequest workoutExerciseRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        NotFound.unless(workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId));

        WorkoutExerciseEntity updatedWorkoutExerciseEntity = workoutService.updateWorkoutExercise(workoutId, workoutExerciseId, workoutExerciseRequest);
        return new ResponseEntity<>(
                workoutExerciseMapper.toResponse(updatedWorkoutExerciseEntity),
                HttpStatus.OK
        );
    }

    @DeleteMapping(path = "/workouts/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(id, userId));
        workoutService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity<Void> deleteWorkoutExercise(@PathVariable("workoutId") Long workoutId, @PathVariable("workoutExerciseId") Long workoutExerciseId, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        NotFound.unless(workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId));
        workoutService.removeExerciseFromWorkout(workoutId, workoutExerciseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets")
    public ResponseEntity<PlannedSetResponse> addPlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId,
            @RequestBody PlannedSetRequest plannedSetRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        NotFound.unless(workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId));
        PlannedSetEntity saved = workoutService.addPlannedSet(workoutExerciseId, plannedSetRequest);
        return new ResponseEntity<>(plannedSetMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets/{setId}")
    public ResponseEntity<PlannedSetResponse> updatePlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId, @PathVariable Long setId,
            @RequestBody PlannedSetRequest plannedSetRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        NotFound.unless(workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId));
        NotFound.unless(workoutService.plannedSetBelongsToWorkoutExercise(setId, workoutExerciseId));
        PlannedSetEntity updated = workoutService.updatePlannedSet(setId, plannedSetRequest);
        return new ResponseEntity<>(plannedSetMapper.toResponse(updated), HttpStatus.OK);
    }

    @DeleteMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets/{setId}")
    public ResponseEntity<Void> deletePlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId, @PathVariable Long setId,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        NotFound.unless(workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId));
        NotFound.unless(workoutService.plannedSetBelongsToWorkoutExercise(setId, workoutExerciseId));
        workoutService.removePlannedSet(workoutExerciseId, setId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Creates an in-progress log from this template. Suggested reps/weight
     * come from {@link com.lukeroche.fit.services.progression.ProgressionService}.
     */
    @PostMapping(path = "/workouts/{workoutId}/start-session")
    public ResponseEntity<WorkoutLogResponse> startSession(
            @PathVariable Long workoutId,
            @RequestBody(required = false) StartSessionRequest startSessionRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutService.isOwnedByUser(workoutId, userId));
        WorkoutLogEntity workoutLog = workoutLogService.startSession(workoutId, userId, startSessionRequest);
        return new ResponseEntity<>(workoutLogMapper.toResponse(workoutLog), HttpStatus.CREATED);
    }
}
