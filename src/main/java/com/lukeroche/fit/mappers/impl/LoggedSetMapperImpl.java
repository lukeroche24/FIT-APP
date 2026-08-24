package com.lukeroche.fit.mappers.impl;

import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetRequest;
import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetResponse;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import com.lukeroche.fit.mappers.LoggedSetMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class LoggedSetMapperImpl implements LoggedSetMapper {

    private ModelMapper modelMapper;

    public LoggedSetMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public LoggedSetResponse toResponse(LoggedSetEntity loggedSetEntity) {
        return modelMapper.map(loggedSetEntity, LoggedSetResponse.class);
    }

    @Override
    public LoggedSetEntity fromRequest(LoggedSetRequest loggedSetRequest) {
        return modelMapper.map(loggedSetRequest, LoggedSetEntity.class);
    }
}
