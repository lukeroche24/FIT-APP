package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalDouble;

@Component
public class ProgressionRecommender {

    private final ProgressionConfig config;

    public ProgressionRecommender(ProgressionConfig config) {
        this.config = config;
    }

    public Recommendation recommend(ProgressionState state,
                                  List<SessionStrength> sessions,
                                  ExerciseEntity exercise,
                                  LoadingScheme scheme,
                                  Integer minReps,
                                  Integer maxReps) {
        if (sessions.isEmpty()) {
            return new Recommendation(state, null, null);
        }

        SessionStrength last = sessions.get(sessions.size() - 1);
        int resolvedMin = minReps == null ? 6 : minReps;
        int resolvedMax = maxReps == null ? 12 : maxReps;
        if (resolvedMin > resolvedMax) {
            int swap = resolvedMin;
            resolvedMin = resolvedMax;
            resolvedMax = swap;
        }

        if (exercise.getLoadingType() == com.lukeroche.fit.domain.entities.LoadingType.BODYWEIGHT) {
            int nextReps = state == ProgressionState.REGRESSING
                    ? Math.max(resolvedMin, last.reps() - 1)
                    : last.reps() + 1;
            return new Recommendation(state, nextReps, 0.0);
        }

        return switch (state) {
            case NEW -> new Recommendation(state, last.reps(), last.weight());
            case PROGRESSING, PLATEAU -> progressOrHold(state, last, resolvedMin, resolvedMax, scheme);
            case REGRESSING -> deload(last, scheme);
        };
    }

    private Recommendation progressOrHold(ProgressionState state,
                                        SessionStrength last,
                                        int minReps,
                                        int maxReps,
                                        LoadingScheme scheme) {
        if (state == ProgressionState.PLATEAU) {
            return new Recommendation(state, last.reps(), last.weight());
        }

        if (last.reps() < maxReps) {
            return new Recommendation(state, last.reps() + 1, last.weight());
        }

        OptionalDouble nextWeight = scheme.nextAbove(last.weight());
        if (nextWeight.isEmpty()) {
            return new Recommendation(state, last.reps(), last.weight());
        }
        return new Recommendation(state, minReps, nextWeight.getAsDouble());
    }

    private Recommendation deload(SessionStrength last, LoadingScheme scheme) {
        double dropped = last.weight() * config.deloadFactor();
        return new Recommendation(ProgressionState.REGRESSING, last.reps(), scheme.nearest(dropped));
    }
}
