package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workout.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.workout.WorkoutExerciseResponse;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;


public interface WorkoutExerciseMapper {

    WorkoutExerciseResponse toResponse(WorkoutExerciseEntity workoutExerciseEntity);

    WorkoutExerciseEntity fromRequest(AddWorkoutExerciseRequest addWorkoutExerciseRequest);
}
