/*
 * Filename: ProgressionRecommender.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.SetTracking;
import com.lukeroche.fit.services.StrengthConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Picks the next prescription from the last session, the workout's rep range,
 * and time off. Same-range work uses double progression (add a rep, then add
 * load). A last session outside the new range is converted via estimated 1RM
 * instead of clamping and adding a plate.
 */
@Component
public class ProgressionRecommender {

    private final ProgressionConfig config;
    private final StrengthConfig strengthConfig;

    public ProgressionRecommender(ProgressionConfig config, StrengthConfig strengthConfig) {
        this.config = config;
        this.strengthConfig = strengthConfig;
    }

    /**
     * Returns the next target reps and weight for this exercise.
     *
     * @param sessions  completed sessions in chronological order
     * @param minReps   workout minimum; {@code null} defaults to 6
     * @param maxReps   workout maximum; {@code null} defaults to 12
     */
    public Recommendation recommend(List<SessionStrength> sessions,
                                  ExerciseEntity exercise,
                                  LoadingScheme scheme,
                                  Integer minReps,
                                  Integer maxReps) {
        if (sessions.isEmpty()) {
            return new Recommendation(null, null);
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
        boolean bodyweightNoLoad = bodyweight && !loadedBodyweight;
        long daysOff = daysSince(last);
        boolean deload = config.deloadAfterDays() > 0 && daysOff >= config.deloadAfterDays();
        boolean outsideRange = last.reps() < resolvedMin
                || (last.reps() > resolvedMax && !last.prescriptionHit());

        if (outsideRange) {
            if (bodyweightNoLoad) {
                int reps = Math.min(Math.max(last.reps(), resolvedMin), resolvedMax);
                if (deload) {
                    return new Recommendation(Math.max(resolvedMin, reps - 1), null);
                }
                return new Recommendation(reps, null);
            }
            if (last.weight() > 0) {
                return convertToReps(sessions, last, resolvedMin, scheme, deload);
            }
        }

        if (deload) {
            if (bodyweightNoLoad) {
                return new Recommendation(Math.max(resolvedMin, last.reps() - 1), null);
            }
            if (loadedBodyweight && last.weight() <= 0) {
                return new Recommendation(Math.max(resolvedMin, last.reps() - 1), 0.0);
            }
            return deload(last, scheme);
        }

        if (!last.prescriptionHit() || (config.holdAfterDays() > 0 && daysOff >= config.holdAfterDays())) {
            return hold(last, bodyweightNoLoad, scheme);
        }

        if (bodyweightNoLoad) {
            return new Recommendation(last.reps() + 1, null);
        }

        return progress(last, resolvedMin, resolvedMax, scheme);
    }

    /**
     * Maps estimated 1RM onto {@code targetReps} with Epley inverted, then snaps
     * to the exercise load step. Applies the deload factor when the user has
     * been off long enough.
     */
    private Recommendation convertToReps(List<SessionStrength> sessions,
                                         SessionStrength last,
                                         int targetReps,
                                         LoadingScheme scheme,
                                         boolean deload) {
        double oneRm = estimatedOneRm(sessions, last);
        double weight = OneRepMax.weightForReps(oneRm, targetReps);
        if (deload) {
            return new Recommendation(targetReps, scheme.nearest(weight * config.deloadFactor()));
        }
        return new Recommendation(targetReps, scheme.nearest(weight));
    }

    /** Best Epley 1RM in the Strength estimated-1RM window, floored at the last session. */
    private double estimatedOneRm(List<SessionStrength> sessions, SessionStrength last) {
        double best = OneRepMax.epley(last.weight(), last.reps());
        int days = Math.max(0, strengthConfig.estimatedOneRmDays());
        LocalDate from = LocalDate.now().minusDays(days);
        for (SessionStrength session : sessions) {
            if (session.completedAt() == null || session.completedAt().toLocalDate().isBefore(from)) {
                continue;
            }
            double estimate = OneRepMax.epley(session.weight(), session.reps());
            if (estimate > best) {
                best = estimate;
            }
        }
        return best;
    }

    private static Recommendation hold(SessionStrength last,
                                       boolean bodyweightNoLoad,
                                       LoadingScheme scheme) {
        if (bodyweightNoLoad) {
            return new Recommendation(last.reps(), null);
        }
        Double weight = last.weight() > 0 ? scheme.nearest(last.weight()) : last.weight();
        return new Recommendation(last.reps(), weight);
    }

    private Recommendation progress(SessionStrength last,
                                   int minReps,
                                   int maxReps,
                                   LoadingScheme scheme) {
        int reps = last.reps();
        if (reps < minReps) {
            reps = minReps;
        } else if (reps > maxReps) {
            reps = maxReps;
        }

        if (reps < maxReps) {
            return new Recommendation(reps + 1, last.weight());
        }

        OptionalDouble nextWeight = scheme.nextAbove(last.weight());
        if (nextWeight.isEmpty()) {
            return new Recommendation(reps, last.weight());
        }
        return new Recommendation(minReps, nextWeight.getAsDouble());
    }

    private static long daysSince(SessionStrength last) {
        if (last.completedAt() == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(last.completedAt().toLocalDate(), LocalDate.now());
        return Math.max(0, days);
    }

    private Recommendation deload(SessionStrength last, LoadingScheme scheme) {
        double dropped = last.weight() * config.deloadFactor();
        return new Recommendation(last.reps(), scheme.nearest(dropped));
    }
}
