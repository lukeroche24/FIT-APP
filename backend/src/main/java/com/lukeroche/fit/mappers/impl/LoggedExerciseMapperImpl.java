package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.workoutlog.AddLoggedExerciseRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedExerciseResponse;
import com.lukeroche.fit.domain.entities.LoggedExerciseEntity;
import com.lukeroche.fit.mappers.LoggedExerciseMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoggedExerciseMapperImpl implements LoggedExerciseMapper {

    private ModelMapper modelMapper;

    public LoggedExerciseMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public LoggedExerciseResponse toResponse(LoggedExerciseEntity loggedExerciseEntity) {
        return modelMapper.map(loggedExerciseEntity, LoggedExerciseResponse.class);
    }

    @Override
    public LoggedExerciseEntity fromRequest(AddLoggedExerciseRequest addLoggedExerciseRequest) {
        return modelMapper.map(addLoggedExerciseRequest, LoggedExerciseEntity.class);
    }
}
