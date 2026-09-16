/*
 * Filename: LoggedSetEntity.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One performed set. {@code actual*} is what the user logged; {@code target*}
 * is what progression asked for and is not overwritten on edit. A fail tick
 * drops the last logged rep, so failing the target rep is a miss and failing
 * a bonus rep is still a hit.
 */
@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "logged_sets", indexes = {
        @Index(name = "idx_logged_sets_logged_exercise", columnList = "logged_exercise_id")
})
public class LoggedSetEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "logged_exercise_id")
    private LoggedExerciseEntity loggedExerciseEntity;

    private Integer setNumber;

    private Integer actualReps;

    private Float actualWeight;

    private Integer rightReps;

    private Float rightWeight;

    private Integer targetReps;

    private Float targetWeight;

    private Integer actualDurationSeconds;

    private Float actualDistance;

    private String notes;

    @Builder.Default
    private Boolean failed = false;

    @Builder.Default
    private Boolean rightFailed = false;

    private LocalDateTime loggedAt;

}
