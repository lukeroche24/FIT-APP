/*
 * Filename: BodyweightLoading.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

/**
 * Pure bodyweight: no external load. Progression is by reps only, so
 * {@link #nextAbove(double)} is empty and snaps are 0.
 */
public final class BodyweightLoading implements LoadingScheme {

    @Override
    public double nearest(double target) {
        return 0;
    }

    @Override
    public OptionalDouble nextAbove(double current) {
        return OptionalDouble.empty();
    }

    @Override
    public double snap(double weight) {
        return 0;
    }
}
