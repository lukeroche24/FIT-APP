package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.SetTracking;
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

        boolean bodyweight = exercise.getLoadingType() == LoadingType.BODYWEIGHT;
        boolean loadedBodyweight = bodyweight && SetTracking.tracksWeight(exercise.getTracksWeight());

        if (last.failed() && state != ProgressionState.REGRESSING) {
            if (bodyweight && !loadedBodyweight) {
                return new Recommendation(state, last.reps(), null);
            }
            if (inRange(last.reps(), resolvedMin, resolvedMax)) {
                return new Recommendation(state, last.reps(), last.weight());
            }
            return convertToMin(state, last, resolvedMin, scheme);
        }

        if (bodyweight && !loadedBodyweight) {
            int nextReps = state == ProgressionState.REGRESSING
                    ? Math.max(resolvedMin, last.reps() - 1)
                    : last.reps() + 1;
            return new Recommendation(state, nextReps, null);
        }

        return switch (state) {
            case NEW, PROGRESSING, PLATEAU -> progressOrHold(state, last, resolvedMin, resolvedMax, scheme);
            case REGRESSING -> loadedBodyweight && last.weight() <= 0
                    ? new Recommendation(state, Math.max(resolvedMin, last.reps() - 1), 0.0)
                    : deload(last, scheme);
        };
    }

    private Recommendation progressOrHold(ProgressionState state,
                                        SessionStrength last,
                                        int minReps,
                                        int maxReps,
                                        LoadingScheme scheme) {
        if (inRange(last.reps(), minReps, maxReps)) {
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

        return convertToMin(state, last, minReps, scheme);
    }

    private static boolean inRange(int reps, int minReps, int maxReps) {
        return reps >= minReps && reps <= maxReps;
    }

    private static Recommendation convertToMin(ProgressionState state,
                                               SessionStrength last,
                                               int minReps,
                                               LoadingScheme scheme) {
        double oneRm = OneRepMax.epley(last.weight(), last.reps());
        double suggested = OneRepMax.weightForReps(oneRm, minReps);
        return new Recommendation(state, minReps, scheme.nearest(suggested));
    }

    private Recommendation deload(SessionStrength last, LoadingScheme scheme) {
        double dropped = last.weight() * config.deloadFactor();
        return new Recommendation(ProgressionState.REGRESSING, last.reps(), scheme.nearest(dropped));
    }
}
