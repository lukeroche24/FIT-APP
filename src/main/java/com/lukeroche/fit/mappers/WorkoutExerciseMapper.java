package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.WorkoutExerciseResponse;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;


public interface WorkoutExerciseMapper {

    WorkoutExerciseResponse toResponse(WorkoutExerciseEntity workoutExerciseEntity);

    WorkoutExerciseEntity fromRequest(AddWorkoutExerciseRequest addWorkoutExerciseRequest);
}
