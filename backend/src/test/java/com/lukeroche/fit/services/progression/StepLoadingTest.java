/*
 * Filename: StepLoadingTest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepLoadingTest {

    private final StepLoading scheme = new StepLoading(2.5);

    @Test
    void rejectsNonPositiveStep() {
        assertThrows(IllegalArgumentException.class, () -> new StepLoading(0));
        assertThrows(IllegalArgumentException.class, () -> new StepLoading(-2.5));
    }

    @Test
    void snapKeepsZeroAtZero() {
        assertEquals(0.0, scheme.snap(0));
        assertEquals(0.0, scheme.snap(-10));
    }

    @Test
    void snapRaisesATinyPositiveWeightToOneStep() {
        assertEquals(2.5, scheme.snap(0.1));
    }

    @Test
    void snapRoundsToTheNearestStep() {
        assertEquals(80.0, scheme.snap(81.1));
        assertEquals(82.5, scheme.snap(81.4));
    }

    @Test
    void nearestUsesOneStepWhenTheTargetIsNotPositive() {
        assertEquals(2.5, scheme.nearest(0));
        assertEquals(2.5, scheme.nearest(-5));
    }

    @Test
    void nextAboveAddsOneStepFromAValueAlreadyOnTheGrid() {
        assertEquals(82.5, scheme.nextAbove(80).orElseThrow());
    }

    @Test
    void nextAboveDoesNotSkipAStepFromJustBelowTheGrid() {
        assertEquals(80.0, scheme.nextAbove(79.9).orElseThrow(), 1e-9);
        assertTrue(scheme.nextAbove(80).isPresent());
    }
}
