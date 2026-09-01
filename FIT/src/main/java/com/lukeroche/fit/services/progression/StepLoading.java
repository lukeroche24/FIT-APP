package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

/**
 * Uniform increment (barbell plates, dumbbell jumps, machine stack).
 * {@code nextAbove} treats a value already on a step as exact so floating
 * error does not skip or repeat a jump.
 */
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
        return snap(target);
    }

    @Override
    public OptionalDouble nextAbove(double current) {
        if (current < 0) {
            current = 0;
        }
        double ticks = current / step;
        if (Math.abs(ticks - Math.round(ticks)) < 1e-6) {
            ticks = Math.round(ticks);
        }
        return OptionalDouble.of(snap((Math.floor(ticks) + 1) * step));
    }

    @Override
    public double snap(double weight) {
        if (weight <= 0) {
            return 0;
        }
        double snapped = Math.round(weight / step) * step;
        if (snapped <= 0) {
            snapped = step;
        }
        return Math.round(snapped * 1000d) / 1000d;
    }
}
