/*
 * Filename: SessionBest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.projections.SetHistoryRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collapses completed sets into one {@link SessionStrength} per workout log.
 * Next-session reps/weight use actuals when every set hit its target, and the
 * prescription when anything missed.
 */
public final class SessionBest {

    private SessionBest() {
    }

    /**
     * Groups {@code sets} by workout log, oldest session first.
     * Logs with no dated attempts are skipped.
     */
    public static List<SessionStrength> toSessions(List<SetHistoryRow> sets, LoadingType loadingType) {
        Map<Long, List<SetHistoryRow>> bySession = new LinkedHashMap<>();
        for (SetHistoryRow set : sets) {
            bySession.computeIfAbsent(set.workoutLogId(), unused -> new ArrayList<>()).add(set);
        }

        List<SessionStrength> sessions = new ArrayList<>();
        for (Map.Entry<Long, List<SetHistoryRow>> entry : bySession.entrySet()) {
            double bestAttempt = 0;
            double bestCompleted = 0;
            LocalDateTime completedAt = null;
            int bestAttemptReps = 0;
            double bestAttemptWeight = 0;
            int bestCompletedReps = 0;
            double bestCompletedWeight = 0;
            Integer prescribedReps = null;
            Double prescribedWeight = null;
            boolean prescriptionHit = true;

            for (SetHistoryRow set : entry.getValue()) {
                if (!attempted(set)) {
                    continue;
                }
                if (prescribedReps == null && set.targetReps() != null && set.targetReps() > 0) {
                    prescribedReps = set.targetReps();
                }
                if (prescribedWeight == null && set.targetWeight() != null && set.targetWeight() > 0) {
                    prescribedWeight = set.targetWeight().doubleValue();
                }
                boolean completed = setCompleted(set);
                if (!completed) {
                    prescriptionHit = false;
                }

                WeakerSide.Side side = WeakerSide.of(
                        set.actualReps(),
                        set.actualWeight(),
                        set.rightReps(),
                        set.rightWeight());
                double value = sessionValue(side, loadingType);
                if (value > bestAttempt) {
                    bestAttempt = value;
                    if (completedAt == null) {
                        completedAt = set.completedAt();
                    }
                    bestAttemptReps = side.reps();
                    bestAttemptWeight = side.weight();
                }
                if (completed && value > bestCompleted) {
                    bestCompleted = value;
                    completedAt = set.completedAt();
                    bestCompletedReps = side.reps();
                    bestCompletedWeight = side.weight();
                }
            }
            if (completedAt == null || (bestCompleted <= 0 && bestAttempt <= 0)) {
                continue;
            }

            int reps;
            double weight;
            if (prescriptionHit && bestCompleted > 0) {
                reps = bestCompletedReps;
                weight = bestCompletedWeight;
            } else {
                reps = prescribedReps != null ? prescribedReps : (bestCompleted > 0 ? bestCompletedReps : bestAttemptReps);
                weight = prescribedWeight != null ? prescribedWeight : (bestCompleted > 0 ? bestCompletedWeight : bestAttemptWeight);
            }
            sessions.add(new SessionStrength(
                    entry.getKey(), completedAt, reps, weight, prescriptionHit));
        }

        sessions.sort(Comparator.comparing(SessionStrength::completedAt));
        return sessions;
    }

    /** True when every attempted set reached its target. Empty leftover sets are ignored. */
    public static boolean prescriptionHit(List<SetHistoryRow> sets) {
        if (sets == null || sets.isEmpty()) {
            return false;
        }
        boolean anyAttempt = false;
        for (SetHistoryRow set : sets) {
            if (!attempted(set)) {
                continue;
            }
            anyAttempt = true;
            if (!setCompleted(set)) {
                return false;
            }
        }
        return anyAttempt;
    }

    static boolean attempted(SetHistoryRow set) {
        return (set.actualReps() != null && set.actualReps() > 0)
                || (set.rightReps() != null && set.rightReps() > 0);
    }

    /**
     * A set counts as complete when every logged side completed {@code targetReps}
     * successful reps. A fail tick means the last logged rep did not count, so
     * 8 + fail at a target of 8 is a miss (failed the 8th) while 9 + fail is a
     * hit (failed the 9th after locking in 8). No target means any positive
     * reps count unless a fail flag is set.
     */
    static boolean setCompleted(SetHistoryRow set) {
        boolean hasLeft = set.actualReps() != null && set.actualReps() > 0;
        boolean hasRight = set.rightReps() != null && set.rightReps() > 0;
        if (!hasLeft && !hasRight) {
            return false;
        }
        Integer target = set.targetReps();
        if (target == null || target <= 0) {
            return !Boolean.TRUE.equals(set.failed()) && !Boolean.TRUE.equals(set.rightFailed());
        }
        if (hasLeft && successfulReps(set.actualReps(), set.failed()) < target) {
            return false;
        }
        return !hasRight || successfulReps(set.rightReps(), set.rightFailed()) >= target;
    }

    /** Logged reps minus the failed last rep, if the fail tick is set. */
    static int successfulReps(Integer reps, Boolean failed) {
        if (reps == null || reps <= 0) {
            return 0;
        }
        return Boolean.TRUE.equals(failed) ? reps - 1 : reps;
    }

    private static double sessionValue(WeakerSide.Side side, LoadingType loadingType) {
        if (loadingType == LoadingType.BODYWEIGHT) {
            return side.weight() * 1000 + side.reps();
        }
        return OneRepMax.epley(side.weight(), side.reps());
    }
}
