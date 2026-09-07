package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.plan.PlanDayResponse;
import com.lukeroche.fit.domain.entities.PlanDayEntity;

public interface PlanDayMapper {

    PlanDayResponse toResponse(PlanDayEntity planDayEntity);
}
