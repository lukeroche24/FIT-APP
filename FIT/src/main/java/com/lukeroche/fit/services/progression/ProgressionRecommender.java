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

@Component
public class ProgressionRecommender {

    private final ProgressionConfig config;
    private final StrengthConfig strengthConfig;

    public ProgressionRecommender(ProgressionConfig config, StrengthConfig strengthConfig) {
        this.config = config;
        this.strengthConfig = strengthConfig;
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
        boolean bodyweightNoLoad = bodyweight && !loadedBodyweight;
        long daysOff = daysSince(last);
        boolean deload = config.deloadAfterDays() > 0 && daysOff >= config.deloadAfterDays();
        boolean outsideRange = last.reps() < resolvedMin || last.reps() > resolvedMax;

        if (outsideRange) {
            if (bodyweightNoLoad) {
                int reps = Math.min(Math.max(last.reps(), resolvedMin), resolvedMax);
                if (deload) {
                    return new Recommendation(ProgressionState.REGRESSING, Math.max(resolvedMin, reps - 1), null);
                }
                return new Recommendation(state, reps, null);
            }
            if (last.weight() > 0) {
                return convertToReps(state, sessions, last, resolvedMin, scheme, deload);
            }
        }

        if (deload) {
            if (bodyweightNoLoad) {
                return new Recommendation(ProgressionState.REGRESSING, Math.max(resolvedMin, last.reps() - 1), null);
            }
            if (loadedBodyweight && last.weight() <= 0) {
                return new Recommendation(ProgressionState.REGRESSING, Math.max(resolvedMin, last.reps() - 1), 0.0);
            }
            return deload(last, scheme);
        }

        if (!last.prescriptionHit() || (config.holdAfterDays() > 0 && daysOff >= config.holdAfterDays())) {
            return hold(state, last, bodyweightNoLoad, scheme);
        }

        if (bodyweightNoLoad) {
            return new Recommendation(state, last.reps() + 1, null);
        }

        return progress(state, last, resolvedMin, resolvedMax, scheme);
    }

    private Recommendation convertToReps(ProgressionState state,
                                         List<SessionStrength> sessions,
                                         SessionStrength last,
                                         int targetReps,
                                         LoadingScheme scheme,
                                         boolean deload) {
        double oneRm = estimatedOneRm(sessions, last);
        double weight = OneRepMax.weightForReps(oneRm, targetReps);
        if (deload) {
            return new Recommendation(
                    ProgressionState.REGRESSING, targetReps, scheme.nearest(weight * config.deloadFactor()));
        }
        return new Recommendation(state, targetReps, scheme.nearest(weight));
    }

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

    private static Recommendation hold(ProgressionState state,
                                       SessionStrength last,
                                       boolean bodyweightNoLoad,
                                       LoadingScheme scheme) {
        if (bodyweightNoLoad) {
            return new Recommendation(state, last.reps(), null);
        }
        Double weight = last.weight() > 0 ? scheme.nearest(last.weight()) : last.weight();
        return new Recommendation(state, last.reps(), weight);
    }

    private Recommendation progress(ProgressionState state,
                                   SessionStrength last,
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
            return new Recommendation(state, reps + 1, last.weight());
        }

        OptionalDouble nextWeight = scheme.nextAbove(last.weight());
        if (nextWeight.isEmpty()) {
            return new Recommendation(state, reps, last.weight());
        }
        return new Recommendation(state, minReps, nextWeight.getAsDouble());
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
        return new Recommendation(ProgressionState.REGRESSING, last.reps(), scheme.nearest(dropped));
    }
}
