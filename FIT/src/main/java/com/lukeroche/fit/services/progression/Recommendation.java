package com.lukeroche.fit.services.progression;

public record Recommendation(
        ProgressionState state,
        Integer targetReps,
        Double targetWeight
) {
}