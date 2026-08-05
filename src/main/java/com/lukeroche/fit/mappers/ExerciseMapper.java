package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.ExerciseRequest;
import com.lukeroche.fit.domain.dto.ExerciseResponse;
import com.lukeroche.fit.domain.entities.ExerciseEntity;

public interface ExerciseMapper {

    ExerciseResponse toResponse(ExerciseEntity exerciseEntity);

    ExerciseEntity fromRequest(ExerciseRequest exerciseRequest);
}
