package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.*;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.mappers.WorkoutExerciseMapper;
import com.lukeroche.fit.mappers.WorkoutMapper;
import com.lukeroche.fit.repositories.WorkoutExerciseRepository;
import com.lukeroche.fit.services.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class WorkoutController {

    private WorkoutService workoutService;

    private WorkoutMapper workoutMapper;

    private WorkoutExerciseMapper workoutExerciseMapper;

    public WorkoutController(WorkoutService workoutService, WorkoutMapper workoutMapper, WorkoutExerciseMapper workoutExerciseMapper) {
        this.workoutService = workoutService;
        this.workoutMapper = workoutMapper;
        this.workoutExerciseMapper = workoutExerciseMapper;
    }


    @PostMapping(path= "/workouts")
    public ResponseEntity<WorkoutResponse> createWorkout(@RequestBody WorkoutRequest workoutRequest) {
        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        WorkoutEntity savedWorkoutEntity = workoutService.save(workoutEntity);

        return new ResponseEntity<>(workoutMapper.toResponse(savedWorkoutEntity), HttpStatus.CREATED);
    }

    @PostMapping(path = "/workouts/{id}/exercises")
    public ResponseEntity<WorkoutExerciseResponse> addWorkoutExercise(@PathVariable Long id, @RequestBody AddWorkoutExerciseRequest request) {

        WorkoutExerciseEntity saved = workoutService.addWorkoutExercise(id, request);
        return new ResponseEntity<>(workoutExerciseMapper.toResponse(saved), HttpStatus.CREATED);
    }


    @GetMapping(path = "/workouts")
    public Page<WorkoutResponse> listWorkouts(Pageable pageable) {
        Page<WorkoutEntity> workouts = workoutService.findAll(pageable);
        return workouts.map(workoutMapper::toResponse);
    }


    @GetMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable("id") Long id) {
        Optional<WorkoutEntity> foundWorkout = workoutService.findOne(id);
        return foundWorkout.map(workoutEntity -> {
            WorkoutResponse workoutResponse = workoutMapper.toResponse(workoutEntity);
            return new ResponseEntity<>(workoutResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> fullUpdateWorkout(@PathVariable("id") Long id,
                                                             @RequestBody WorkoutRequest workoutRequest) {
        if (!workoutService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        };

        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        workoutEntity.setId(id);
        WorkoutEntity savedWorkoutEntity = workoutService.save(workoutEntity);

        return new ResponseEntity<>(workoutMapper.toResponse(savedWorkoutEntity), HttpStatus.OK);

    }

    @PatchMapping(path = "/workouts/{id}")
    public ResponseEntity<WorkoutResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody WorkoutRequest workoutRequest) {
        if(!workoutService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        WorkoutEntity updatedWorkout = workoutService.partialUpdate(id, workoutEntity);
        return new ResponseEntity<>(
                workoutMapper.toResponse(updatedWorkout),
                HttpStatus.OK
        );
    }

    @PatchMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity<WorkoutExerciseResponse> reorderWorkoutExercises(
            @PathVariable("workoutId") Long workoutId,
            @PathVariable("workoutExerciseId") Long workoutExerciseId,
            @RequestBody UpdateWorkoutExerciseRequest workoutExerciseRequest) {
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
    public ResponseEntity deleteWorkout(@PathVariable("id") Long id) {
        workoutService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/workouts/{workoutId}/exercises/{workoutExerciseId}")
    public ResponseEntity deleteWorkoutExercise(@PathVariable("workoutId") Long workoutId, @PathVariable("workoutExerciseId") Long workoutExerciseId) {
        if(!workoutService.workoutExerciseBelongsToWorkout(workoutExerciseId, workoutId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        };
        workoutService.removeExerciseFromWorkout(workoutId, workoutExerciseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
