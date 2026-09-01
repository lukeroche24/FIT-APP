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
 * Trend uses the best successful set. Next-session reps/weight use actuals
 * when every set hit its target, and the prescription when anything missed,
 * so a typed 105 kg 1RM is not discarded in favour of a leftover planned load.
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
                // Repeat the written prescription, not the short or failed actuals.
                reps = prescribedReps != null ? prescribedReps : (bestCompleted > 0 ? bestCompletedReps : bestAttemptReps);
                weight = prescribedWeight != null ? prescribedWeight : (bestCompleted > 0 ? bestCompletedWeight : bestAttemptWeight);
            }
            double trendValue = bestCompleted > 0 ? bestCompleted : bestAttempt;
            sessions.add(new SessionStrength(
                    entry.getKey(), completedAt, trendValue, reps, weight, prescriptionHit));
        }

        sessions.sort(Comparator.comparing(SessionStrength::completedAt));
        return sessions;
    }

    /**
     * A set counts as complete when it is not marked failed and every logged
     * side reached {@code targetReps}. No target means any positive reps count.
     */
    static boolean setCompleted(SetHistoryRow set) {
        if (Boolean.TRUE.equals(set.failed()) || Boolean.TRUE.equals(set.rightFailed())) {
            return false;
        }
        Integer target = set.targetReps();
        if (target == null || target <= 0) {
            return true;
        }
        boolean hasLeft = set.actualReps() != null && set.actualReps() > 0;
        boolean hasRight = set.rightReps() != null && set.rightReps() > 0;
        if (!hasLeft && !hasRight) {
            return false;
        }
        if (hasLeft && set.actualReps() < target) {
            return false;
        }
        return !hasRight || set.rightReps() >= target;
    }

    private static double sessionValue(WeakerSide.Side side, LoadingType loadingType) {
        if (loadingType == LoadingType.BODYWEIGHT) {
            // Rank added load first, then reps, so 5 kg × 8 beats 0 kg × 12.
            return side.weight() * 1000 + side.reps();
        }
        return OneRepMax.epley(side.weight(), side.reps());
    }
}
