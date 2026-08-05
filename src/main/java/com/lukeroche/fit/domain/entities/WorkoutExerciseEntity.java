package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"plannedSets"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "workout_exercises")
public class WorkoutExerciseEntity extends BaseEntity{
    private Integer orderIndex;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private WorkoutEntity workoutEntity;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exerciseEntity;

    @OneToMany(mappedBy = "workoutExerciseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlannedSetEntity> plannedSets = new ArrayList<>();


}
