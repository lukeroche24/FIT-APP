/*
 * Filename: Laterality.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.domain.entities;

/**
 * Limb pattern and independent-load defaults. Missing pattern is bilateral.
 * Missing independentLoads is true only for dumbbells.
 */
public final class Laterality {

    private Laterality() {
    }

    public static LimbPattern pattern(LimbPattern value) {
        return value == null ? LimbPattern.BILATERAL : value;
    }

    public static LimbPattern resolvePattern(LimbPattern requested, LimbPattern fallback) {
        return requested != null ? requested : pattern(fallback);
    }

    public static boolean isUnilateral(LimbPattern value) {
        return pattern(value) == LimbPattern.UNILATERAL;
    }

    public static boolean independentLoads(Boolean stored, LoadingType loadingType) {
        if (stored != null) {
            return stored;
        }
        return loadingType == LoadingType.DUMBBELL;
    }

    public static boolean resolveIndependentLoads(Boolean requested, Boolean fallback, LoadingType loadingType) {
        if (requested != null) {
            return requested;
        }
        return independentLoads(fallback, loadingType);
    }
}
