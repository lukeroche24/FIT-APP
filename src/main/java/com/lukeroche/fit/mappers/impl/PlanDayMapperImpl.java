package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.plan.PlanDayResponse;
import com.lukeroche.fit.domain.entities.PlanDayEntity;
import com.lukeroche.fit.mappers.PlanDayMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PlanDayMapperImpl implements PlanDayMapper {

    private ModelMapper modelMapper;

    public PlanDayMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PlanDayResponse toResponse(PlanDayEntity planDayEntity) {
        return modelMapper.map(planDayEntity, PlanDayResponse.class);
    }
}
