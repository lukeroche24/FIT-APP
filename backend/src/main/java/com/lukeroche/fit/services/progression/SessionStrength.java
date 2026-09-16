/*
 * Filename: SessionStrength.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import java.time.LocalDateTime;

/**
 * One completed workout log, collapsed to a single strength point for the
 * next-session recommender.
 *
 * @param reps             actual reps used for the next prescription when the
 *                         session hit; prescribed reps when it missed
 * @param weight           actual load when the session hit; prescribed load when it missed
 * @param prescriptionHit  every working set reached its target without a fail flag
 */
public record SessionStrength(
        Long workoutLogId,
        LocalDateTime completedAt,
        int reps,
        double weight,
        boolean prescriptionHit
) {
}
