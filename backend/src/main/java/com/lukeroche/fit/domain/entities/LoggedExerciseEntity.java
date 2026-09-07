package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * An exercise on a logged session. Tracking and laterality can differ from
 * the library exercise for this session only.
 */
@Getter
@Setter
@ToString(exclude = {"loggedSets"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "logged_exercises", indexes = {
        @Index(name = "idx_logged_exercises_workout_log", columnList = "workout_log_id"),
        @Index(name = "idx_logged_exercises", columnList = "exercise_id")
})
public class LoggedExerciseEntity extends BaseEntity {

    private Long orderIndex;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "workout_log_id")
    private WorkoutLogEntity workoutLogEntity;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exerciseEntity;

    @Builder.Default
    private Boolean tracksWeight = true;

    @Builder.Default
    private Boolean tracksDuration = false;

    @Builder.Default
    private Boolean tracksDistance = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LimbPattern limbPattern = LimbPattern.BILATERAL;

    @Builder.Default
    private Boolean independentLoads = false;

    @OneToMany(mappedBy = "loggedExerciseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @Builder.Default
    private List<LoggedSetEntity> loggedSets = new ArrayList<>();

}
