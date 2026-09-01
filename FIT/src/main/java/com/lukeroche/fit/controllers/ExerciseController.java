package com.lukeroche.fit.controllers;


import com.lukeroche.fit.NotFound;
import com.lukeroche.fit.domain.dto.exercise.ExerciseRequest;
import com.lukeroche.fit.domain.dto.exercise.ExerciseResponse;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.mappers.ExerciseMapper;
import com.lukeroche.fit.services.ExerciseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * HTTP for the caller's exercise library. Exercises are private to the owner.
 * Unowned ids return 404.
 */
@RestController
public class ExerciseController {

    private ExerciseService exerciseService;

    private ExerciseMapper exerciseMapper;

    public ExerciseController(ExerciseService exerciseService, ExerciseMapper exerciseMapper) {
        this.exerciseService = exerciseService;
        this.exerciseMapper = exerciseMapper;
    }


    @PostMapping(path= "/exercises")
    public ResponseEntity<ExerciseResponse> createExercise(@RequestBody ExerciseRequest exercise, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        ExerciseEntity exerciseEntity = exerciseMapper.fromRequest(exercise);
        exerciseEntity.setCreatedByUserId(userId);
        ExerciseEntity savedExerciseEntity = exerciseService.save(exerciseEntity);

        return new ResponseEntity<>(exerciseMapper.toResponse(savedExerciseEntity), HttpStatus.CREATED);
    }


    @GetMapping(path = "/exercises")
    public Page<ExerciseResponse> listExercises(
            @RequestParam(required = false) String query,
            Pageable pageable,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<ExerciseEntity> exercises = exerciseService.findAllForUser(userId, query, pageable);
        return exercises.map(exerciseMapper::toResponse);
    }


    @GetMapping(path = "/exercises/{id}")
    public ResponseEntity<ExerciseResponse> getExercise(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        ExerciseEntity exercise = exerciseService.findOneForUser(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found"));
        return new ResponseEntity<>(exerciseMapper.toResponse(exercise), HttpStatus.OK);
    }

    @PatchMapping(path = "/exercises/{id}")
    public ResponseEntity<ExerciseResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody ExerciseRequest exerciseRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(exerciseService.isOwnedByUser(id, userId));

        ExerciseEntity exerciseEntity = exerciseMapper.fromRequest(exerciseRequest);
        ExerciseEntity updatedExercise = exerciseService.partialUpdate(id, userId, exerciseEntity);
        return new ResponseEntity<>(
                exerciseMapper.toResponse(updatedExercise),
                HttpStatus.OK
        );
    }

    @DeleteMapping(path = "/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        NotFound.unless(exerciseService.isOwnedByUser(id, userId));
        exerciseService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
