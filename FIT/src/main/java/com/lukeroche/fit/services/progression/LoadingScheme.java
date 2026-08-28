package com.lukeroche.fit.services.progression;

import java.util.OptionalDouble;

public interface LoadingScheme {

    double nearest(double target);

    OptionalDouble nextAbove(double current);

    double snap(double weight);
}
