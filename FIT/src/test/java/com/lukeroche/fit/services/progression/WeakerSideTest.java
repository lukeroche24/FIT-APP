package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeakerSideTest {

    @Test
    void usesTheOnlyLoggedSide() {
        assertEquals(new WeakerSide.Side(8, 40), WeakerSide.of(8, 40f, null, null));
        assertEquals(new WeakerSide.Side(6, 35), WeakerSide.of(null, null, 6, 35f));
    }

    @Test
    void missingBothSidesIsZero() {
        assertEquals(new WeakerSide.Side(0, 0), WeakerSide.of(null, null, null, null));
    }

    @Test
    void picksTheLighterSideThenFewerRepsAtTheSameWeight() {
        assertEquals(new WeakerSide.Side(8, 20), WeakerSide.of(8, 24f, 8, 20f));
        assertEquals(new WeakerSide.Side(8, 20), WeakerSide.of(8, 20f, 8, 24f));
        assertEquals(new WeakerSide.Side(6, 24), WeakerSide.of(8, 24f, 6, 24f));
    }
}
