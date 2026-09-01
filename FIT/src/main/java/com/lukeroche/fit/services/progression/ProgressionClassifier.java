package com.lukeroche.fit.services.progression;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Labels an exercise from its recent estimated-1RM trend, then overrides that
 * label after a layoff (hold, then deload).
 */
@Component
public class ProgressionClassifier {

    private final ProgressionConfig config;

    public ProgressionClassifier(ProgressionConfig config) {
        this.config = config;
    }

    /**
     * {@code NEW} with fewer than two sessions. Otherwise slope vs
     * {@code slopeEpsilon}, or {@code PLATEAU} after enough consecutive stalls.
     */
    public ProgressionState classify(List<SessionStrength> sessions, Trend trend) {
        if (trend.sampleSize() < 2) {
            return ProgressionState.NEW;
        }

        if (trend.slope() > config.slopeEpsilon()) {
            return ProgressionState.PROGRESSING;
        }
        if (trend.slope() < -config.slopeEpsilon()) {
            return ProgressionState.REGRESSING;
        }

        int stalls = countTrailingStalls(sessions);
        if (stalls >= config.stallsBeforePlateau()) {
            return ProgressionState.PLATEAU;
        }
        return ProgressionState.PROGRESSING;
    }

    /**
     * Time-off overlay: {@code deloadAfterDays} forces {@code REGRESSING},
     * {@code holdAfterDays} forces {@code PLATEAU}. Shorter gaps leave
     * {@code state} unchanged.
     */
    public ProgressionState applyRecency(ProgressionState state, List<SessionStrength> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return state;
        }
        SessionStrength last = sessions.get(sessions.size() - 1);
        if (last.completedAt() == null) {
            return state;
        }
        long days = ChronoUnit.DAYS.between(last.completedAt().toLocalDate(), LocalDate.now());
        if (days < 0) {
            return state;
        }
        if (config.deloadAfterDays() > 0 && days >= config.deloadAfterDays()) {
            return ProgressionState.REGRESSING;
        }
        if (config.holdAfterDays() > 0 && days >= config.holdAfterDays()) {
            return ProgressionState.PLATEAU;
        }
        return state;
    }

    private static int countTrailingStalls(List<SessionStrength> sessions) {
        int stalls = 0;
        for (int i = sessions.size() - 1; i > 0; i--) {
            if (sessions.get(i).value() <= sessions.get(i - 1).value()) {
                stalls++;
            } else {
                break;
            }
        }
        return stalls;
    }
}
