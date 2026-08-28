package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

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
