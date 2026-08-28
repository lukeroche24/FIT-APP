package com.lukeroche.fit.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fit.strength")
public record StrengthConfig(

        int estimatedOneRmDays
) {}
