package com.lukeroche.fit.services.progression;

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
