/*
 * Filename: WorkoutEntity.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Workout template: ordered exercises and planned sets. Starting a session
 * clones this into a {@link WorkoutLogEntity}.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "workouts")
public class WorkoutEntity extends BaseEntity{

    @Column(name = "created_by_id", nullable = false)
    private UUID createdByUserId;

    private String description;

    private String name;

    @OneToMany(mappedBy = "workoutEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<WorkoutExerciseEntity> workoutExercises = new ArrayList<>();

}
