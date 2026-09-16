/*
 * Filename: ProgressionConfig.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.services.progression;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunables for layoff behaviour, bound from {@code fit.progression.*} in
 * {@code application.properties}.
 *
 * @param deloadFactor    fraction of last load after a long layoff (e.g. 0.9)
 * @param holdAfterDays   days off before repeating last load (0 disables)
 * @param deloadAfterDays days off before cutting load (0 disables)
 */
@ConfigurationProperties(prefix = "fit.progression")
public record ProgressionConfig(

        double deloadFactor,

        int holdAfterDays,

        int deloadAfterDays
) {}
