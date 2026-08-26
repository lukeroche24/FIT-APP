package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import com.lukeroche.fit.repositories.LoggedSetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public Recommendation forExercise(UUID userId, ExerciseEntity exercise) {
        return forExercise(userId, exercise, null, null);
    }

    public Recommendation forExercise(UUID userId, ExerciseEntity exercise, Integer minReps, Integer maxReps) {
        List<SetHistoryRow> sets =
                loggedSetRepository.findCompletedSetHistory(userId, exercise.getId());
        List<SessionStrength> sessions = SessionBest.toSessions(sets, exercise.getLoadingType());
        Trend trend = TrendFit.fit(sessions, config.windowSize());
        ProgressionState state = classifier.classify(sessions, trend);
        LoadingScheme scheme = loadingSchemeFactory.forExercise(exercise);
        return recommender.recommend(state, sessions, exercise, scheme, minReps, maxReps);
    }
}
