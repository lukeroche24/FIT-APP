package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.projections.SetHistoryRow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SessionBest {

    private SessionBest() {
    }

    public static List<SessionStrength> toSessions(List<SetHistoryRow> sets, LoadingType loadingType) {
        Map<Long, List<SetHistoryRow>> bySession = new LinkedHashMap<>();
        for (SetHistoryRow set : sets) {
            bySession.computeIfAbsent(set.workoutLogId(), unused -> new ArrayList<>()).add(set);
        }

        List<SessionStrength> sessions = new ArrayList<>();
        for (Map.Entry<Long, List<SetHistoryRow>> entry : bySession.entrySet()) {
            double best = 0;
            LocalDateTime completedAt = null;
            int bestReps = 0;
            double bestWeight = 0;
            for (SetHistoryRow set : entry.getValue()) {
                double value = sessionValue(set, loadingType);
                if (value > best) {
                    best = value;
                    completedAt = set.completedAt();
                    bestReps = set.actualReps() == null ? 0 : set.actualReps();
                    bestWeight = set.actualWeight() == null ? 0 : set.actualWeight();
                }
            }
            if (best > 0 && completedAt != null) {
                sessions.add(new SessionStrength(entry.getKey(), completedAt, best, bestReps, bestWeight));
            }
        }

        sessions.sort(Comparator.comparing(SessionStrength::completedAt));
        return sessions;
    }

    private static double sessionValue(SetHistoryRow set, LoadingType loadingType) {
        int reps = set.actualReps() == null ? 0 : set.actualReps();
        double weight = set.actualWeight() == null ? 0 : set.actualWeight();
        if (loadingType == LoadingType.BODYWEIGHT) {
            return reps;
        }
        return OneRepMax.epley(weight, reps);
    }
}
