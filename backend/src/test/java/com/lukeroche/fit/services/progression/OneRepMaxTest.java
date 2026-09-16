/*
 * Filename: OneRepMaxTest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OneRepMaxTest {

    @Test
    void aSingleIsTheOneRm() {
        assertEquals(100.0, OneRepMax.epley(100, 1));
        assertEquals(100.0, OneRepMax.weightForReps(100, 1));
    }

    @Test
    void epleyAddsAThirtiethPerRep() {
        assertEquals(100.0 * (1 + 5 / 30.0), OneRepMax.epley(100, 5), 1e-9);
    }

    @Test
    void weightForRepsInvertsEpley() {
        double oneRm = OneRepMax.epley(80, 8);
        assertEquals(80.0, OneRepMax.weightForReps(oneRm, 8), 1e-9);
    }

    @Test
    void nonPositiveInputsAreZero() {
        assertEquals(0.0, OneRepMax.epley(0, 8));
        assertEquals(0.0, OneRepMax.epley(80, 0));
        assertEquals(0.0, OneRepMax.weightForReps(0, 8));
        assertEquals(0.0, OneRepMax.weightForReps(100, 0));
    }
}
