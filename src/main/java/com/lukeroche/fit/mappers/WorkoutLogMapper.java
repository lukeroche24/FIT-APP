package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workoutlog.WorkoutLogRequest;
import com.lukeroche.fit.domain.dto.workoutlog.WorkoutLogResponse;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;

public interface WorkoutLogMapper {

    WorkoutLogResponse toResponse(WorkoutLogEntity workoutLogEntity);

    WorkoutLogEntity fromRequest(WorkoutLogRequest workoutLogRequest);
}
