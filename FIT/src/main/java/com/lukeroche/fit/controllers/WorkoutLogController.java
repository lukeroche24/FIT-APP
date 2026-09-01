package com.lukeroche.fit.controllers;

import com.lukeroche.fit.NotFound;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * HTTP for logged sessions. Caller id comes from the JWT filter.
 * Mutations require ownership. GET uses {@link WorkoutLogService#findVisibleToUser}
 * so a friend can read a completed log; in-progress stays private.
 * Missing or foreign ids return 404 rather than 403 so existence is not leaked.
 */
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
    public Page<WorkoutLogResponse> listWorkoutLogs(
            @RequestParam(required = false) String query,
            Pageable pageable,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<WorkoutLogEntity> workoutLogs = workoutLogService.findAllForUser(userId, query, pageable);
        return workoutLogs.map(workoutLogMapper::toResponse);
    }

    /** {@code 404} when nothing is in progress; that is the empty state, not an error. */
    @GetMapping(path = "/workout-logs/in-progress")
    public ResponseEntity<InProgressSessionResponse> getInProgressSession(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        return workoutLogService.findInProgressForUser(userId)
                .map(log -> ResponseEntity.ok(InProgressSessionResponse.builder()
                        .id(log.getId())
                        .name(log.getName())
                        .startedAt(log.getStartedAt())
                        .sourceWorkoutId(log.getSourceWorkoutId())
                        .build()))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Owner always; friends only after {@code completedAt}. {@code \\d+} so this does not steal {@code /in-progress}. */
    @GetMapping(path = "/workout-logs/{id:\\d+}")
    public ResponseEntity<WorkoutLogResponse> getWorkoutLog(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        WorkoutLogEntity log = workoutLogService.findVisibleToUser(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        return new ResponseEntity<>(workoutLogMapper.toResponse(log), HttpStatus.OK);
    }

    @PatchMapping(path = "/workout-logs/{id}")
    public ResponseEntity<WorkoutLogResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody WorkoutLogRequest workoutLogRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.isOwnedByUser(id, userId));
        WorkoutLogEntity workoutLogEntity = workoutLogMapper.fromRequest(workoutLogRequest);
        WorkoutLogEntity updatedLog = workoutLogService.partialUpdate(id, userId, workoutLogEntity);
        return new ResponseEntity<>(workoutLogMapper.toResponse(updatedLog), HttpStatus.OK);
    }

    /** Stamps {@code completedAt}; the log then appears in history and the friends feed. */
    @PostMapping(path = "/workout-logs/{id}/finish")
    public ResponseEntity<WorkoutLogResponse> finishSession(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.isOwnedByUser(id, userId));
        WorkoutLogEntity finished = workoutLogService.finishSession(id, userId);
        return new ResponseEntity<>(workoutLogMapper.toResponse(finished), HttpStatus.OK);
    }

    @DeleteMapping(path = "/workout-logs/{id}")
    public ResponseEntity<Void> deleteWorkoutLog(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.isOwnedByUser(id, userId));
        workoutLogService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/workout-logs/{id}/exercises")
    public ResponseEntity<LoggedExerciseResponse> addLoggedExercise(
            @PathVariable("id") Long id,
            @RequestBody AddLoggedExerciseRequest addRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.isOwnedByUser(id, userId));
        NotFound.unless(exerciseService.isOwnedByUser(addRequest.getExerciseId(), userId));
        LoggedExerciseEntity saved = workoutLogService.addLoggedExercise(id, userId, addRequest);
        return new ResponseEntity<>(loggedExerciseMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/workout-logs/{workoutLogId}/exercises/{loggedExerciseId}")
    public ResponseEntity<Void> removeLoggedExercise(
            @PathVariable("workoutLogId") Long workoutLogId,
            @PathVariable("loggedExerciseId") Long loggedExerciseId,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.isOwnedByUser(workoutLogId, userId));
        NotFound.unless(workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId));
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
        NotFound.unless(workoutLogService.isOwnedByUser(workoutLogId, userId));
        NotFound.unless(workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId));
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
        NotFound.unless(workoutLogService.isOwnedByUser(workoutLogId, userId));
        NotFound.unless(workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId));
        NotFound.unless(workoutLogService.loggedSetBelongsToLoggedExercise(setId, loggedExerciseId));
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
        NotFound.unless(workoutLogService.isOwnedByUser(workoutLogId, userId));
        NotFound.unless(workoutLogService.loggedExerciseBelongsToWorkoutLog(loggedExerciseId, workoutLogId));
        NotFound.unless(workoutLogService.loggedSetBelongsToLoggedExercise(setId, loggedExerciseId));
        workoutLogService.removeLoggedSet(loggedExerciseId, setId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /** Clones a completed log (own or a friend's) into the caller's workout templates. */
    @PostMapping(path = "/workout-logs/{id}/copy")
    public ResponseEntity<WorkoutResponse> copyToLibrary(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(workoutLogService.canCopy(id, userId));
        WorkoutEntity copy = workoutService.copyWorkoutLogToLibrary(id, userId);
        return new ResponseEntity<>(workoutMapper.toResponse(copy), HttpStatus.CREATED);
    }
}
