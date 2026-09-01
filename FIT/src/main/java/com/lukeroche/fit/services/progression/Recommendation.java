package com.lukeroche.fit.services.progression;

/**
 * Next-session suggestion produced by {@link ProgressionRecommender}.
 * {@code targetReps} / {@code targetWeight} are {@code null} when there is no
 * history yet.
 */
public record Recommendation(
        ProgressionState state,
        Integer targetReps,
        Double targetWeight
) {
}
