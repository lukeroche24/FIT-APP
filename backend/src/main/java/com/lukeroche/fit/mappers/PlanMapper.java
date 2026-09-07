package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.plan.PlanRequest;
import com.lukeroche.fit.domain.dto.plan.PlanResponse;
import com.lukeroche.fit.domain.entities.PlanEntity;

public interface PlanMapper {

    PlanResponse toResponse(PlanEntity planEntity);

    PlanEntity fromRequest(PlanRequest planRequest);
}
