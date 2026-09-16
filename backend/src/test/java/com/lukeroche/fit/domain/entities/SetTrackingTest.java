/*
 * Filename: SetTrackingTest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

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
