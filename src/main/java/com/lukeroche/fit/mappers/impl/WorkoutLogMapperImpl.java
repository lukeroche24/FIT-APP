package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.workoutlog.WorkoutLogRequest;
import com.lukeroche.fit.domain.dto.workoutlog.WorkoutLogResponse;
import com.lukeroche.fit.domain.entities.WorkoutLogEntity;
import com.lukeroche.fit.mappers.WorkoutLogMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class WorkoutLogMapperImpl implements WorkoutLogMapper {

    private ModelMapper modelMapper;

    public WorkoutLogMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public WorkoutLogResponse toResponse(WorkoutLogEntity workoutLogEntity) {
        return modelMapper.map(workoutLogEntity, WorkoutLogResponse.class);
    }

    @Override
    public WorkoutLogEntity fromRequest(WorkoutLogRequest workoutLogRequest) {
        return modelMapper.map(workoutLogRequest, WorkoutLogEntity.class);
    }
}
