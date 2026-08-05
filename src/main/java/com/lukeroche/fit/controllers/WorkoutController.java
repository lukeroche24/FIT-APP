package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.WorkoutRequest;
import com.lukeroche.fit.domain.dto.WorkoutResponse;
import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.mappers.Mapper;
import com.lukeroche.fit.mappers.WorkoutMapper;
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

    public WorkoutController(WorkoutService workoutService, WorkoutMapper workoutMapper) {
        this.workoutService = workoutService;
        this.workoutMapper = workoutMapper;
    }


    @PostMapping(path= "/workouts")
    public ResponseEntity<WorkoutResponse> createWorkout(@RequestBody WorkoutRequest workoutRequest) {
        WorkoutEntity workoutEntity = workoutMapper.fromRequest(workoutRequest);
        WorkoutEntity savedWorkoutEntity = workoutService.save(workoutEntity);

        return new ResponseEntity<>(workoutMapper.toResponse(savedWorkoutEntity), HttpStatus.CREATED);
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

    @DeleteMapping(path = "/workouts/{id}")
    public ResponseEntity deleteWorkout(@PathVariable("id") Long id) {
        workoutService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
