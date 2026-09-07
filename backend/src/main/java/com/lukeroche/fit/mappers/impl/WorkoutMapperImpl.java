package com.lukeroche.fit.mappers.impl;


import com.lukeroche.fit.domain.dto.workout.WorkoutRequest;
import com.lukeroche.fit.domain.dto.workout.WorkoutResponse;

import com.lukeroche.fit.domain.entities.WorkoutEntity;
import com.lukeroche.fit.mappers.WorkoutMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class WorkoutMapperImpl implements WorkoutMapper {

    private ModelMapper modelMapper;


    public WorkoutMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public WorkoutResponse toResponse(WorkoutEntity workoutEntity) {
        return modelMapper.map(workoutEntity, WorkoutResponse.class);
    }

    @Override
    public WorkoutEntity fromRequest(WorkoutRequest workoutRequest) {
        return modelMapper.map(workoutRequest, WorkoutEntity.class);
    }
    
}
