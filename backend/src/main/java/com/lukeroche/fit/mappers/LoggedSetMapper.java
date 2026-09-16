/*
 * Filename: LoggedSetMapper.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.mappers;

import com.lukeroche.fit.domain.dto.workoutlog.LoggedSetResponse;
import com.lukeroche.fit.domain.entities.LoggedSetEntity;

public interface LoggedSetMapper {

    LoggedSetResponse toResponse(LoggedSetEntity loggedSetEntity);
}
