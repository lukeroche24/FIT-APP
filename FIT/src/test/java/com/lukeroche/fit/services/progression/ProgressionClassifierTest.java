package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressionClassifierTest {

    private final ProgressionClassifier classifier =
            new ProgressionClassifier(new ProgressionConfig(5, 0.25, 3, 0.9, 14, 28));

    @Test
    void isNewUntilTwoSessionsExist() {
        assertEquals(ProgressionState.NEW, classifier.classify(List.of(), new Trend(0, 0)));
        assertEquals(ProgressionState.NEW, classifier.classify(List.of(valued(100)), new Trend(1, 5)));
    }

    @Test
    void progressingWhenSlopeIsUp() {
        assertEquals(
                ProgressionState.PROGRESSING,
                classifier.classify(List.of(valued(100), valued(110), valued(120)), new Trend(3, 1)));
    }

    @Test
    void regressingWhenSlopeIsDown() {
        assertEquals(
                ProgressionState.REGRESSING,
                classifier.classify(List.of(valued(120), valued(110), valued(100)), new Trend(3, -1)));
    }

    @Test
    void plateauAfterEnoughTrailingStallsOnAFlatSlope() {
        assertEquals(
                ProgressionState.PLATEAU,
                classifier.classify(
                        List.of(valued(100), valued(100), valued(100), valued(100)),
                        new Trend(4, 0)));
    }

    @Test
    void stillProgressingIfFlatButNotEnoughStalls() {
        assertEquals(
                ProgressionState.PROGRESSING,
                classifier.classify(List.of(valued(100), valued(110), valued(110)), new Trend(3, 0)));
    }

    @Test
    void holdsAfterTwoWeeksOff() {
        SessionStrength last = session(LocalDateTime.now().minusDays(16));
        ProgressionState next = classifier.applyRecency(ProgressionState.PROGRESSING, List.of(last));
        assertEquals(ProgressionState.PLATEAU, next);
    }

    @Test
    void deloadsAfterFourWeeksOff() {
        SessionStrength last = session(LocalDateTime.now().minusDays(30));
        ProgressionState next = classifier.applyRecency(ProgressionState.PROGRESSING, List.of(last));
        assertEquals(ProgressionState.REGRESSING, next);
    }

    @Test
    void leavesRecentSessionsAlone() {
        SessionStrength last = session(LocalDateTime.now().minusDays(3));
        ProgressionState next = classifier.applyRecency(ProgressionState.PROGRESSING, List.of(last));
        assertEquals(ProgressionState.PROGRESSING, next);
    }

    @Test
    void emptyHistoryLeavesTheTrendLabel() {
        assertEquals(
                ProgressionState.PROGRESSING,
                classifier.applyRecency(ProgressionState.PROGRESSING, List.of()));
    }

    private static SessionStrength session(LocalDateTime completedAt) {
        return new SessionStrength(1L, completedAt, 100, 8, 100, true);
    }

    private static SessionStrength valued(double value) {
        return new SessionStrength(1L, LocalDateTime.now(), value, 8, 80, true);
    }
}
