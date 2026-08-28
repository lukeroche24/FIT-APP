package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetResponse;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;

public interface LoggedSetMapper {

    LoggedSetResponse toResponse(LoggedSetEntity loggedSetEntity);

    //LoggedSetEntity fromRequest(LoggedSetRequest loggedSetRequest);
}
