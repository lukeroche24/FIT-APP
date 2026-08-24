package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workout.WorkoutRequest;
import com.lukeroche.fit.domain.dto.workout.WorkoutResponse;
import com.lukeroche.fit.domain.entities.WorkoutEntity;

public interface WorkoutMapper {

    WorkoutResponse toResponse(WorkoutEntity workoutEntity);

    WorkoutEntity fromRequest(WorkoutRequest workoutRequest);
}
