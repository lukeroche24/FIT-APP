package com.lukeroche.fit.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LateralityTest {

    @Test
    void missingPatternIsBilateral() {
        assertEquals(LimbPattern.BILATERAL, Laterality.pattern(null));
        assertFalse(Laterality.isUnilateral(null));
        assertTrue(Laterality.isUnilateral(LimbPattern.UNILATERAL));
    }

    @Test
    void resolvePatternPrefersTheRequestThenTheFallback() {
        assertEquals(LimbPattern.UNILATERAL, Laterality.resolvePattern(LimbPattern.UNILATERAL, LimbPattern.BILATERAL));
        assertEquals(LimbPattern.ALTERNATING, Laterality.resolvePattern(null, LimbPattern.ALTERNATING));
        assertEquals(LimbPattern.BILATERAL, Laterality.resolvePattern(null, null));
    }

    @Test
    void independentLoadsDefaultToDumbbellOnly() {
        assertTrue(Laterality.independentLoads(null, LoadingType.DUMBBELL));
        assertFalse(Laterality.independentLoads(null, LoadingType.BARBELL));
        assertFalse(Laterality.independentLoads(false, LoadingType.DUMBBELL));
        assertTrue(Laterality.independentLoads(true, LoadingType.BARBELL));
    }

    @Test
    void resolveIndependentLoadsPrefersTheRequest() {
        assertTrue(Laterality.resolveIndependentLoads(true, false, LoadingType.BARBELL));
        assertTrue(Laterality.resolveIndependentLoads(null, null, LoadingType.DUMBBELL));
        assertFalse(Laterality.resolveIndependentLoads(null, false, LoadingType.DUMBBELL));
    }
}
