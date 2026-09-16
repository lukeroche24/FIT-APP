/*
 * Filename: ProgressionService.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import com.lukeroche.fit.repositories.LoggedSetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Entry point for next-session suggestions. Loads completed set history for an
 * exercise, collapses it to one point per session, then asks
 * {@link ProgressionRecommender} for target reps and weight.
 */
@Service
public class ProgressionService {

    private final LoggedSetRepository loggedSetRepository;
    private final LoadingSchemeFactory loadingSchemeFactory;
    private final ProgressionRecommender recommender;

    public ProgressionService(LoggedSetRepository loggedSetRepository,
                              LoadingSchemeFactory loadingSchemeFactory,
                              ProgressionRecommender recommender) {
        this.loggedSetRepository = loggedSetRepository;
        this.loadingSchemeFactory = loadingSchemeFactory;
        this.recommender = recommender;
    }

    /**
     * Suggests the next load using the default hypertrophy range (6–12) when
     * the workout does not specify min/max reps.
     */
    public Recommendation forExercise(UUID userId, ExerciseEntity exercise) {
        return forExercise(userId, exercise, null, null);
    }

    /**
     * Suggests the next load for {@code exercise}, constrained to
     * {@code minReps}–{@code maxReps} on the workout. A {@code null} range is
     * treated as 6–12.
     */
    public Recommendation forExercise(UUID userId, ExerciseEntity exercise, Integer minReps, Integer maxReps) {
        List<SetHistoryRow> sets =
                loggedSetRepository.findCompletedSetHistory(userId, exercise.getId());
        List<SessionStrength> sessions = SessionBest.toSessions(sets, exercise.getLoadingType());
        LoadingScheme scheme = loadingSchemeFactory.forExercise(exercise);
        return recommender.recommend(sessions, exercise, scheme, minReps, maxReps);
    }
}
