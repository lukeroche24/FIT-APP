package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"loggedSets"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "logged_exercises")
public class LoggedExerciseEntity extends BaseEntity {

    private Long orderIndex;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "workout_log_id")
    private WorkoutLogEntity workoutLogEntity;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exerciseEntity;

    @OneToMany(mappedBy = "loggedExerciseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoggedSetEntity> loggedSets = new ArrayList<>();

}
