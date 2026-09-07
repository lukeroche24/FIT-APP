package com.lukeroche.fit.services.progression;

/** Trend of recent session strength: {@code NEW} until two data points exist. */
public enum ProgressionState {
    NEW,
    PROGRESSING,
    PLATEAU,
    REGRESSING
}
