package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

/**
 * How an exercise can actually be loaded. Implementations snap to plate,
 * stack, or dumbbell increments, or ignore load for pure bodyweight work.
 */
public interface LoadingScheme {

    /** Closest loadable weight to {@code target}; never below the smallest positive step. */
    double nearest(double target);

    /** Next increment strictly above {@code current}, if one exists. */
    OptionalDouble nextAbove(double current);

    /** Round {@code weight} onto this scheme. Zero and negatives stay 0. */
    double snap(double weight);
}
