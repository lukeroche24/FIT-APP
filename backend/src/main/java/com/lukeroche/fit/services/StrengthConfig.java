package com.lukeroche.fit.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Window for estimated 1RM, bound from {@code fit.strength.*}.
 *
 * @param estimatedOneRmDays days of completed history to scan for the best
 *                           Epley estimate (default 28). Tested 1RM and
 *                           heaviest lift ignore this and use all-time.
 */
@ConfigurationProperties(prefix = "fit.strength")
public record StrengthConfig(

        int estimatedOneRmDays
) {}
