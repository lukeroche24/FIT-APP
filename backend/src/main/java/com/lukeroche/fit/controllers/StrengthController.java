/*
 * Filename: StrengthController.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.controllers;

import com.lukeroche.fit.domain.dto.user.StrengthExerciseOption;
import com.lukeroche.fit.domain.dto.user.StrengthStatsResponse;
import com.lukeroche.fit.services.StrengthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Estimated 1RM, tested 1RM, and heaviest lift for a user. Self or a friend;
 * anyone else gets 404 from {@link StrengthService}.
 */
@RestController
public class StrengthController {

    private final StrengthService strengthService;

    public StrengthController(StrengthService strengthService) {
        this.strengthService = strengthService;
    }

    @GetMapping(path = "/users/{id}/strength/exercises")
    public List<StrengthExerciseOption> listTrainedExercises(@PathVariable("id") UUID id,
                                                             HttpServletRequest request) {
        UUID viewerId = (UUID) request.getAttribute("userId");
        return strengthService.listTrainedExercises(viewerId, id);
    }

    @GetMapping(path = "/users/{id}/strength/exercises/{exerciseId}")
    public StrengthStatsResponse getStats(@PathVariable("id") UUID id,
                                          @PathVariable("exerciseId") Long exerciseId,
                                          HttpServletRequest request) {
        UUID viewerId = (UUID) request.getAttribute("userId");
        return strengthService.getStats(viewerId, id, exerciseId);
    }
}
