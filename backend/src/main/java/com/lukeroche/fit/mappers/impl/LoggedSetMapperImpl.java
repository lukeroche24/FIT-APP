/*
 * Filename: LoggedSetMapperImpl.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.mappers.impl;

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
}
