package com.lukeroche.fit.services.progression;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fit.progression")
public record ProgressionConfig(

        int windowSize,

        double slopeEpsilon,

        int stallsBeforePlateau,

        double deloadFactor
) {}
