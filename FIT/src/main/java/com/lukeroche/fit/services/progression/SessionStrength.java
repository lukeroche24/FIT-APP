package com.lukeroche.fit.services.progression;

import java.time.LocalDateTime;

/**
 * One completed workout log, collapsed to a single strength point for trend
 * fitting and the next-session recommender.
 *
 * @param value            estimated 1RM (Epley) of the best successful set, or
 *                         a bodyweight ranking of load then reps
 * @param reps             actual reps used for the next prescription when the
 *                         session hit; prescribed reps when it missed
 * @param weight           actual load when the session hit; prescribed load when it missed
 * @param prescriptionHit  every working set reached its target without a fail flag
 */
public record SessionStrength(
        Long workoutLogId,
        LocalDateTime completedAt,
        double value,
        int reps,
        double weight,
        boolean prescriptionHit
) {
}
