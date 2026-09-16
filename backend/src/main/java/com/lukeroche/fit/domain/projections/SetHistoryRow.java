/*
 * Filename: SetHistoryRow.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.domain.projections;

import java.time.LocalDateTime;

/**
 * One completed set for an exercise, used by progression and strength stats.
 * {@code actual*} is what was lifted; {@code target*} is the prescription
 * seeded at session start. Failed flags exclude the side from both engines.
 */
public record SetHistoryRow(

    Long workoutLogId,
    LocalDateTime completedAt,
    Integer setNumber,
    Integer actualReps,
    Float actualWeight,
    Integer rightReps,
    Float rightWeight,
    Integer targetReps,
    Float targetWeight,
    Boolean failed,
    Boolean rightFailed) {

}
