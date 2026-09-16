/*
 * Filename: Recommendation.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

/**
 * Next-session suggestion produced by {@link ProgressionRecommender}.
 * {@code targetReps} / {@code targetWeight} are {@code null} when there is no
 * history yet.
 */
public record Recommendation(
        Integer targetReps,
        Double targetWeight
) {
}
