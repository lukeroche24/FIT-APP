package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.ExerciseRequest;
import com.lukeroche.fit.domain.dto.ExerciseResponse;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.mappers.ExerciseMapper;
import com.lukeroche.fit.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapperImpl implements ExerciseMapper {

    private ModelMapper modelMapper;

    public ExerciseMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseResponse toResponse(ExerciseEntity exerciseEntity) {
        return modelMapper.map(exerciseEntity, ExerciseResponse.class);
    }

    @Override
    public ExerciseEntity fromRequest(ExerciseRequest exerciseRequest) {
        return modelMapper.map(exerciseRequest, ExerciseEntity.class);
    }

}
