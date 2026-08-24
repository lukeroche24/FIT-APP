package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workout.PlannedSetRequest;
import com.lukeroche.fit.domain.dto.workout.PlannedSetResponse;
import com.lukeroche.fit.domain.entities.PlannedSetEntity;

public interface PlannedSetMapper {

    PlannedSetResponse toResponse(PlannedSetEntity plannedSetEntity);

    PlannedSetEntity fromRequest(PlannedSetRequest plannedSetRequest);
}
