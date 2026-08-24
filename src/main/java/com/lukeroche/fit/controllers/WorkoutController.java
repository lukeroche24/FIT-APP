package com.lukeroche.fit.controllers;

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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

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
        if (!workoutService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!exerciseService.isOwnedByUser(addRequest.getExerciseId(), userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WorkoutExerciseEntity saved = workoutService.addWorkoutExercise(id, userId, addRequest);
        return new ResponseEntity<>(workoutExerciseMapper.toResponse(saved), HttpStatus.CREATED);
    }


    @GetMapping(path = "/workouts")
    public Page<WorkoutResponse> listWorkouts(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<WorkoutEntity> workouts = workoutService.findAllForUser(userId, pageable);
        return workouts.map(workoutMapper::toResponse);
    }


    @GetMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Optional<WorkoutEntity> foundWorkout = workoutService.findOneForUser(id, userId);
        return foundWorkout.map(workoutEntity -> {
            WorkoutResponse workoutResponse = workoutMapper.toResponse(workoutEntity);
            return new ResponseEntity<>(workoutResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> fullUpdateWorkout(@PathVariable("id") Long id,
                                                             @RequestBody WorkoutRequest workoutRequest,
                                                             HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        };

        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        workoutEntity.setId(id);
        workoutEntity.setCreatedByUserId(userId);
        WorkoutEntity savedWorkoutEntity = workoutService.save(workoutEntity);

        return new ResponseEntity<>(workoutMapper.toResponse(savedWorkoutEntity), HttpStatus.OK);

    }

    @PatchMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody WorkoutRequest workoutRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if(!workoutService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        WorkoutEntity updatedWorkout = workoutService.partialUpdate(id, userId, workoutEntity);
        return new ResponseEntity<>(
                workoutMapper.toResponse(updatedWorkout),
                HttpStatus.OK
        );
    }

    @PatchMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> reorderWorkoutExercises(
            @PathVariable("workoutId") Long workoutId,
            @PathVariable("workoutExerciseId") Long workoutExerciseId,
            @RequestBody UpdateWorkoutExerciseRequest workoutExerciseRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WorkoutExerciseEntity updatedWorkoutExerciseEntity = workoutService.reorderWorkoutExercise(workoutId, workoutExerciseId, workoutExerciseRequest);
        return new ResponseEntity<>(
                workoutExerciseMapper.toResponse(updatedWorkoutExerciseEntity),
                HttpStatus.OK
        );
    }

    @DeleteMapping(path = "/workouts/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        workoutService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity<Void> deleteWorkoutExercise(@PathVariable("workoutId") Long workoutId, @PathVariable("workoutExerciseId") Long workoutExerciseId, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        };
        workoutService.removeExerciseFromWorkout(workoutId, workoutExerciseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets")
    public ResponseEntity<PlannedSetResponse> addPlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId,
            @RequestBody PlannedSetRequest plannedSetRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        PlannedSetEntity saved = workoutService.addPlannedSet(workoutExerciseId, plannedSetRequest);
        return new ResponseEntity<>(plannedSetMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets/{setId}")
    public ResponseEntity<PlannedSetResponse> updatePlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId, @PathVariable Long setId,
            @RequestBody PlannedSetRequest plannedSetRequest, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutService.plannedSetBelongsToWorkoutExercise(setId, workoutExerciseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        PlannedSetEntity updated = workoutService.updatePlannedSet(setId, plannedSetRequest);
        return new ResponseEntity<>(plannedSetMapper.toResponse(updated), HttpStatus.OK);
    }

    @DeleteMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}/sets/{setId}")
    public ResponseEntity<Void> deletePlannedSet(
            @PathVariable Long workoutId, @PathVariable Long workoutExerciseId, @PathVariable Long setId,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutService.plannedSetBelongsToWorkoutExercise(setId, workoutExerciseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        workoutService.removePlannedSet(workoutExerciseId, setId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workouts/{workoutId}/start-session")
    public ResponseEntity<WorkoutLogResponse> startSession(
            @PathVariable Long workoutId,
            @RequestBody(required = false) StartSessionRequest startSessionRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutService.isOwnedByUser(workoutId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        WorkoutLogEntity workoutLog = workoutLogService.startSession(workoutId, userId, startSessionRequest);
        return new ResponseEntity<>(workoutLogMapper.toResponse(workoutLog), HttpStatus.CREATED);
    }
}
