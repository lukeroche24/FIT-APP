package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.AddWorkoutExerciseRequest;
import com.lukeroche.fit.domain.dto.WorkoutExerciseResponse;
import com.lukeroche.fit.domain.entities.WorkoutExerciseEntity;
import com.lukeroche.fit.mappers.WorkoutExerciseMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class WorkoutExerciseMapperImpl implements WorkoutExerciseMapper {

    private ModelMapper modelMapper;


    public WorkoutExerciseMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public WorkoutExerciseResponse toResponse(WorkoutExerciseEntity workoutExerciseEntity) {
        return modelMapper.map(workoutExerciseEntity, WorkoutExerciseResponse.class);
    }

    @Override
    public WorkoutExerciseEntity fromRequest(AddWorkoutExerciseRequest addWorkoutExerciseRequest) {
        return modelMapper.map(addWorkoutExerciseRequest, WorkoutExerciseEntity.class);
    }
}
