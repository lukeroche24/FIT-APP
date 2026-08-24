package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.workout.WorkoutResponse;
import com.lukeroche.fit.domain.dto.workoutlog.*;
import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.mappers.LoggedExerciseMapper;
import com.lukeroche.fit.mappers.LoggedSetMapper;
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
public class WorkoutLogController {

    private WorkoutLogService workoutLogService;

    private ExerciseService exerciseService;

    private WorkoutService workoutService;

    private WorkoutLogMapper workoutLogMapper;

    private LoggedExerciseMapper loggedExerciseMapper;

    private LoggedSetMapper loggedSetMapper;

    private WorkoutMapper workoutMapper;

    public WorkoutLogController(WorkoutLogService workoutLogService, ExerciseService exerciseService, WorkoutService workoutService, WorkoutLogMapper workoutLogMapper, LoggedExerciseMapper loggedExerciseMapper, LoggedSetMapper loggedSetMapper, WorkoutMapper workoutMapper) {
        this.workoutLogService = workoutLogService;
        this.exerciseService = exerciseService;
        this.workoutService = workoutService;
        this.workoutLogMapper = workoutLogMapper;
        this.loggedExerciseMapper = loggedExerciseMapper;
        this.loggedSetMapper = loggedSetMapper;
        this.workoutMapper = workoutMapper;
    }

    @GetMapping(path = "/workout-logs")
    public Page<WorkoutLogResponse> listWorkoutLogs(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<WorkoutLogEntity> workoutLogs = workoutLogService.findAllForUser(userId, pageable);
        return workoutLogs.map(workoutLogMapper::toResponse);
    }

    @GetMapping(path = "/workout-logs/{id}")
    public ResponseEntity<WorkoutLogResponse> getWorkoutLog(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Optional<WorkoutLogEntity> foundLog = workoutLogService.findOneForUser(id, userId);
        return foundLog.map(workoutLogEntity -> {
            WorkoutLogResponse workoutLogResponse = workoutLogMapper.toResponse(workoutLogEntity);
            return new ResponseEntity<>(workoutLogResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PatchMapping(path = "/workout-logs/{id}")
    public ResponseEntity<WorkoutLogResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody WorkoutLogRequest workoutLogRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        WorkoutLogEntity workoutLogEntity = workoutLogMapper.fromRequest(workoutLogRequest);
        WorkoutLogEntity updatedLog = workoutLogService.partialUpdate(id, userId, workoutLogEntity);
        return new ResponseEntity<>(workoutLogMapper.toResponse(updatedLog), HttpStatus.OK);
    }

    @PostMapping(path = "/workout-logs/{id}/finish")
    public ResponseEntity<WorkoutLogResponse> finishSession(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        WorkoutLogEntity finished = workoutLogService.finishSession(id, userId);
        return new ResponseEntity<>(workoutLogMapper.toResponse(finished), HttpStatus.OK);
    }

    @DeleteMapping(path = "/workout-logs/{id}")
    public ResponseEntity<Void> deleteWorkoutLog(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        workoutLogService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workout-logs/{id}/exercises")
    public ResponseEntity<LoggedExerciseResponse> addLoggedExercise(
            @PathVariable("id") Long id,
            @RequestBody AddLoggedExerciseRequest addRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!exerciseService.isOwnedByUser(addRequest.getExerciseId(), userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        LoggedExerciseEntity saved = workoutLogService.addLoggedExercise(id, userId, addRequest);
        return new ResponseEntity<>(loggedExerciseMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/workout-logs/{workoutLogId}/exercises/{loggedExerciseId}")
    public ResponseEntity<Void> removeLoggedExercise(
            @PathVariable("workoutLogId") Long workoutLogId,
            @PathVariable("loggedExerciseId") Long loggedExerciseId,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(workoutLogId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        workoutLogService.removeLoggedExercise(workoutLogId, loggedExerciseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workout-logs/{workoutLogId}/exercises/{loggedExerciseId}/sets")
    public ResponseEntity<LoggedSetResponse> addLoggedSet(
            @PathVariable("workoutLogId") Long workoutLogId,
            @PathVariable("loggedExerciseId") Long loggedExerciseId,
            @RequestBody LoggedSetRequest loggedSetRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(workoutLogId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        LoggedSetEntity saved = workoutLogService.addLoggedSet(loggedExerciseId, loggedSetRequest);
        return new ResponseEntity<>(loggedSetMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @PatchMapping(path = "/workout-logs/{workoutLogId}/exercises/{loggedExerciseId}/sets/{setId}")
    public ResponseEntity<LoggedSetResponse> updateLoggedSet(
            @PathVariable("workoutLogId") Long workoutLogId,
            @PathVariable("loggedExerciseId") Long loggedExerciseId,
            @PathVariable("setId") Long setId,
            @RequestBody LoggedSetRequest loggedSetRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(workoutLogId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedSetBelongsToLoggedExercise(setId, loggedExerciseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        LoggedSetEntity updated = workoutLogService.updateLoggedSet(setId, loggedSetRequest);
        return new ResponseEntity<>(loggedSetMapper.toResponse(updated), HttpStatus.OK);
    }

    @DeleteMapping(path = "/workout-logs/{workoutLogId}/exercises/{loggedExerciseId}/sets/{setId}")
    public ResponseEntity<Void> removeLoggedSet(
            @PathVariable("workoutLogId") Long workoutLogId,
            @PathVariable("loggedExerciseId") Long loggedExerciseId,
            @PathVariable("setId") Long setId,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.isOwnedByUser(workoutLogId, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!workoutLogService.loggedSetBelongsToLoggedExercise(setId, loggedExerciseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        workoutLogService.removeLoggedSet(loggedExerciseId, setId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workout-logs/{id}/copy")
    public ResponseEntity<WorkoutResponse> copyToLibrary(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!workoutLogService.canCopy(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        WorkoutEntity copy = workoutService.copyWorkoutLogToLibrary(id, userId);
        return new ResponseEntity<>(workoutMapper.toResponse(copy), HttpStatus.CREATED);
    }
}
