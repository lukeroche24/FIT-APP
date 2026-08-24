package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.exercise.ExerciseRequest;
import com.lukeroche.fit.domain.dto.exercise.ExerciseResponse;
import com.lukeroche.fit.domain.entities.ExerciseEntity;

public interface ExerciseMapper {

    ExerciseResponse toResponse(ExerciseEntity exerciseEntity);

    ExerciseEntity fromRequest(ExerciseRequest exerciseRequest);
}
