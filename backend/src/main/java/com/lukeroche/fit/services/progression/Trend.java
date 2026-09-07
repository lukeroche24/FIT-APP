package com.lukeroche.fit.services.progression;

/**
 * Ordinary-least-squares slope of estimated 1RM over a trailing window of
 * sessions. {@code x} is session index in that window, not calendar time.
 *
 * @param sampleSize sessions in the fit
 * @param slope      change in estimated 1RM per session
 */
public record Trend(int sampleSize, double slope) {
}
