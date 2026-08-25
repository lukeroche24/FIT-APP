package com.lukeroche.fit.domain.projections;

import java.time.LocalDateTime;

public record SetHistoryRow(

    Long workoutLogId,
    LocalDateTime completedAt,
    Integer setNumber,
    Integer actualReps,
    Float actualWeight) {

}
