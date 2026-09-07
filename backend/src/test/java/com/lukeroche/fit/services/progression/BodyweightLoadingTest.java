package com.lukeroche.fit.services.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyweightLoadingTest {

    private final BodyweightLoading scheme = new BodyweightLoading();

    @Test
    void hasNoExternalLoad() {
        assertEquals(0.0, scheme.nearest(20));
        assertEquals(0.0, scheme.snap(5));
        assertTrue(scheme.nextAbove(0).isEmpty());
    }
}
