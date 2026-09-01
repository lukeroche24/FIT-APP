package com.lukeroche.fit.services.progression;

/**
 * Epley estimated 1RM and its inverse. A single is taken as the 1RM; more reps
 * use {@code weight × (1 + reps / 30)}.
 */
public class OneRepMax {

    private OneRepMax() {
    }

    public static double epley(double weight, int reps) {
        if(weight <= 0 || reps <= 0) {
            return 0.0;
        }

        if(reps == 1) {
            return weight;
        }

        return (weight * (1.0+( reps /30.0)));
    }

    /**
     * Load that would produce {@code oneRm} at {@code reps}, inverting
     * {@link #epley(double, int)}.
     */
    static double weightForReps(double oneRm, int reps) {
        if (oneRm <= 0 || reps <= 0){
            return 0.0;
        }

        if(reps == 1){
            return oneRm;
        }

        return oneRm / (1 + reps/30.0);
    }


}
