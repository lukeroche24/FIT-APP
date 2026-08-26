package com.lukeroche.fit.services.progression;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgressionClassifier {

    private final ProgressionConfig config;

    public ProgressionClassifier(ProgressionConfig config) {
        this.config = config;
    }

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
