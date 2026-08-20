package com.lukeroche.fit.controllers;


import com.lukeroche.fit.domain.dto.ExerciseRequest;
import com.lukeroche.fit.domain.dto.ExerciseResponse;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.mappers.ExerciseMapper;
import com.lukeroche.fit.services.ExerciseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
public class ExerciseController {

    private ExerciseService exerciseService;

    private ExerciseMapper exerciseResponseMapper;

    private ExerciseMapper exerciseRequestMapper;

    public ExerciseController(ExerciseService exerciseService, ExerciseMapper exerciseResponseMapper, ExerciseMapper exerciseRequestMapper) {
        this.exerciseService = exerciseService;
        this.exerciseResponseMapper = exerciseResponseMapper;
        this.exerciseRequestMapper = exerciseRequestMapper;
    }


    @PostMapping(path= "/exercises")
    public ResponseEntity<ExerciseResponse> createExercise(@RequestBody ExerciseRequest exercise, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        ExerciseEntity exerciseEntity = exerciseRequestMapper.fromRequest(exercise);
        exerciseEntity.setCreatedByUserId(userId);
        ExerciseEntity savedExerciseEntity = exerciseService.save(exerciseEntity);

        return new ResponseEntity<>(exerciseResponseMapper.toResponse(savedExerciseEntity), HttpStatus.CREATED);
    }


    @GetMapping(path = "/exercises")
    public Page<ExerciseResponse> listExercises(Pageable pageable, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Page<ExerciseEntity> exercises = exerciseService.findAllForUser(userId, pageable);
        return exercises.map(exerciseResponseMapper::toResponse);
    }


    @GetMapping(path = "/exercises/{id}")
    public ResponseEntity<ExerciseResponse> getExercise(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        Optional<ExerciseEntity> foundExercise = exerciseService.findOneForUser(id, userId);
        return foundExercise.map(exerciseEntity -> {
            ExerciseResponse exerciseResponse = exerciseResponseMapper.toResponse(exerciseEntity);
            return new ResponseEntity<>(exerciseResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/exercises/{id}")
    public ResponseEntity<ExerciseResponse> fullUpdateExercise(@PathVariable("id") Long id,
                                                               @RequestBody ExerciseRequest exerciseRequest,
                                                               HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!exerciseService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        };

        ExerciseEntity exerciseEntity = exerciseRequestMapper.fromRequest(exerciseRequest);
        exerciseEntity.setId(id);
        exerciseEntity.setCreatedByUserId(userId);
        ExerciseEntity savedExerciseEntity = exerciseService.save(exerciseEntity);

        return new ResponseEntity<>(exerciseResponseMapper.toResponse(savedExerciseEntity), HttpStatus.OK);

    }

    @PatchMapping(path = "/exercises/{id}")
    public ResponseEntity<ExerciseResponse> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody ExerciseRequest exerciseRequest,
            HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if(!exerciseService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ExerciseEntity exerciseEntity = exerciseRequestMapper.fromRequest(exerciseRequest);
        ExerciseEntity updatedExercise = exerciseService.partialUpdate(id, userId, exerciseEntity);
        return new ResponseEntity<>(
                exerciseResponseMapper.toResponse(updatedExercise),
                HttpStatus.OK
        );
    }

    @DeleteMapping(path = "/exercises/{id}")
    public ResponseEntity deleteExercise(@PathVariable("id") Long id, HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (!exerciseService.isOwnedByUser(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        exerciseService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
