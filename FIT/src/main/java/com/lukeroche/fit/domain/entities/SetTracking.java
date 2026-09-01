package com.lukeroche.fit.domain.entities;

/**
 * Which metrics a set records. Weight defaults on (null counts as tracked);
 * duration and distance default off.
 */
public final class SetTracking {

    private SetTracking() {
    }

    public static boolean tracksWeight(Boolean value) {
        return value == null || value;
    }

    public static boolean tracksDuration(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static boolean tracksDistance(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static boolean resolveWeight(Boolean requested, Boolean fallback) {
        return requested != null ? requested : tracksWeight(fallback);
    }

    public static boolean resolveDuration(Boolean requested, Boolean fallback) {
        return requested != null ? requested : tracksDuration(fallback);
    }

    public static boolean resolveDistance(Boolean requested, Boolean fallback) {
        return requested != null ? requested : tracksDistance(fallback);
    }
}
