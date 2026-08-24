package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workout.PlannedSetResponse;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;
import com.lukeroche.fit.mappers.PlannedSetMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PlannedSetMapperImpl implements PlannedSetMapper {

    private ModelMapper modelMapper;

    public PlannedSetMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PlannedSetResponse toResponse(PlannedSetEntity plannedSetEntity) {
        return modelMapper.map(plannedSetEntity, PlannedSetResponse.class);
    }

    @Override
    public PlannedSetEntity fromRequest(PlannedSetRequest plannedSetRequest) {
        return modelMapper.map(plannedSetRequest, PlannedSetEntity.class);
    }
}
