package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.plan.PlanRequest;
import com.lukeroche.fit.domain.dto.plan.PlanResponse;
import com.lukeroche.fit.domain.entities.PlanEntity;
import com.lukeroche.fit.mappers.PlanMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PlanMapperImpl implements PlanMapper {

    private ModelMapper modelMapper;

    public PlanMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public PlanResponse toResponse(PlanEntity planEntity) {
        return modelMapper.map(planEntity, PlanResponse.class);
    }

    @Override
    public PlanEntity fromRequest(PlanRequest planRequest) {
        return modelMapper.map(planRequest, PlanEntity.class);
    }
}
