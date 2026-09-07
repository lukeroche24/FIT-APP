package com.lukeroche.fit.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetTrackingTest {

    @Test
    void weightDefaultsOn() {
        assertTrue(SetTracking.tracksWeight(null));
        assertTrue(SetTracking.tracksWeight(true));
        assertFalse(SetTracking.tracksWeight(false));
    }

    @Test
    void durationAndDistanceDefaultOff() {
        assertFalse(SetTracking.tracksDuration(null));
        assertFalse(SetTracking.tracksDuration(false));
        assertTrue(SetTracking.tracksDuration(true));
        assertFalse(SetTracking.tracksDistance(null));
        assertTrue(SetTracking.tracksDistance(true));
    }

    @Test
    void resolvePrefersTheRequestThenTheFallbackDefault() {
        assertFalse(SetTracking.resolveWeight(false, true));
        assertTrue(SetTracking.resolveWeight(null, null));
        assertTrue(SetTracking.resolveDuration(true, false));
        assertFalse(SetTracking.resolveDuration(null, null));
        assertTrue(SetTracking.resolveDistance(null, true));
        assertFalse(SetTracking.resolveDistance(null, null));
    }
}
