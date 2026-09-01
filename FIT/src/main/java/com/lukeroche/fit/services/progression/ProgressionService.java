package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import com.lukeroche.fit.repositories.LoggedSetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Entry point for next-session suggestions. Loads completed set history for an
 * exercise, collapses it to one point per session, classifies the trend, then
 * asks {@link ProgressionRecommender} for target reps and weight.
 */
@Service
public class ProgressionService {

    private final LoggedSetRepository loggedSetRepository;
    private final ProgressionConfig config;
    private final ProgressionClassifier classifier;
    private final LoadingSchemeFactory loadingSchemeFactory;
    private final ProgressionRecommender recommender;

    public ProgressionService(LoggedSetRepository loggedSetRepository,
                              ProgressionConfig config,
                              ProgressionClassifier classifier,
                              LoadingSchemeFactory loadingSchemeFactory,
                              ProgressionRecommender recommender) {
        this.loggedSetRepository = loggedSetRepository;
        this.config = config;
        this.classifier = classifier;
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
        Trend trend = TrendFit.fit(sessions, config.windowSize());
        ProgressionState state = classifier.applyRecency(classifier.classify(sessions, trend), sessions);
        LoadingScheme scheme = loadingSchemeFactory.forExercise(exercise);
        return recommender.recommend(state, sessions, exercise, scheme, minReps, maxReps);
    }
}
