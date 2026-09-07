package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrendFitTest {

    @Test
    void needsTwoSessionsForASlope() {
        Trend trend = TrendFit.fit(List.of(session(100)), 5);
        assertEquals(1, trend.sampleSize());
        assertEquals(0.0, trend.slope());
    }

    @Test
    void risingValuesHaveAPositiveSlope() {
        Trend trend = TrendFit.fit(List.of(session(100), session(110), session(120)), 5);
        assertEquals(3, trend.sampleSize());
        assertTrue(trend.slope() > 0);
    }

    @Test
    void fallingValuesHaveANegativeSlope() {
        Trend trend = TrendFit.fit(List.of(session(120), session(110), session(100)), 5);
        assertTrue(trend.slope() < 0);
    }

    @Test
    void windowKeepsOnlyTheMostRecentSessions() {
        Trend full = TrendFit.fit(List.of(session(50), session(100), session(110), session(120)), 3);
        assertEquals(3, full.sampleSize());
        assertTrue(full.slope() > 0);
    }

    private static SessionStrength session(double value) {
        return new SessionStrength(1L, LocalDateTime.now(), value, 8, 80, true);
    }
}
