package com.lukeroche.fit.services.progression;

import java.time.LocalDateTime;

public record SessionStrength(
        Long workoutLogId,
        LocalDateTime completedAt,
        double value,
        int reps,
        double weight,
        boolean prescriptionHit
) {
}
