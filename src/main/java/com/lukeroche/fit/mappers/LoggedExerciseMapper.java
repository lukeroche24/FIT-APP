package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workoutlog.AddLoggedExerciseRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedExerciseResponse;
import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;

public interface LoggedExerciseMapper {

    LoggedExerciseResponse toResponse(LoggedExerciseEntity loggedExerciseEntity);

    LoggedExerciseEntity fromRequest(AddLoggedExerciseRequest addLoggedExerciseRequest);
}
