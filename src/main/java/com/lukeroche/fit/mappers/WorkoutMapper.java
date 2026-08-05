package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.WorkoutRequest;
import com.lukeroche.fit.domain.dto.WorkoutResponse;
import com.lukeroche.fit.domain.entities.WorkoutEntity;

public interface WorkoutMapper {

    WorkoutResponse toResponse(WorkoutEntity workoutEntity);

    WorkoutEntity fromRequest(WorkoutRequest workoutRequest);
}
