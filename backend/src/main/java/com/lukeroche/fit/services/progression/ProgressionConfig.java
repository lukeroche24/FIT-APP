package com.lukeroche.fit.services.progression;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunables for trend classification and layoff behaviour, bound from
 * {@code fit.progression.*} in {@code application.properties}.
 *
 * @param windowSize          recent sessions used for the slope
 * @param slopeEpsilon        |slope| below this is treated as flat
 * @param stallsBeforePlateau consecutive non-improving sessions for {@code PLATEAU}
 * @param deloadFactor        fraction of last load after a long layoff (e.g. 0.9)
 * @param holdAfterDays       days off before repeating last load (0 disables)
 * @param deloadAfterDays     days off before cutting load (0 disables)
 */
@ConfigurationProperties(prefix = "fit.progression")
public record ProgressionConfig(

        int windowSize,

        double slopeEpsilon,

        int stallsBeforePlateau,

        double deloadFactor,

        int holdAfterDays,

        int deloadAfterDays
) {}
