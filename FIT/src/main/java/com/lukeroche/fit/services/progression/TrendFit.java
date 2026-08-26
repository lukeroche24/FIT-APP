package com.lukeroche.fit.services.progression;

import java.util.List;

public final class TrendFit {

    private TrendFit() {
    }

    public static Trend fit(List<SessionStrength> sessions, int windowSize) {
        int from = Math.max(0, sessions.size() - windowSize);
        List<SessionStrength> window = sessions.subList(from, sessions.size());
        int n = window.size();
        if (n < 2) {
            return new Trend(n, 0);
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        for (int i = 0; i < n; i++) {
            double y = window.get(i).value();
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumXX += i * i;
        }

        double denominator = n * sumXX - sumX * sumX;
        double slope = denominator == 0 ? 0 : (n * sumXY - sumX * sumY) / denominator;
        return new Trend(n, slope);
    }
}
