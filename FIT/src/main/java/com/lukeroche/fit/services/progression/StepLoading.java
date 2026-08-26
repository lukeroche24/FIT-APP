package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

public final class StepLoading implements LoadingScheme {

    private final double step;

    public StepLoading(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Load step must be greater than zero, got " + step);
        }
        this.step = step;
    }

    @Override
    public double nearest(double target) {
        if (target <= 0) {
            return step;
        }
        return Math.round(target / step) * step;
    }

    @Override
    public OptionalDouble nextAbove(double current) {
        return OptionalDouble.of((Math.floor(current / step) + 1) * step);
    }
}
