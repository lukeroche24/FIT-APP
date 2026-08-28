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

    private Long orderIndex;

    //private String notes;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private WorkoutEntity workoutEntity;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private ExerciseEntity exerciseEntity;

    private Integer minReps;

    private Integer maxReps;

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

    @OneToMany(mappedBy = "workoutExerciseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @Builder.Default
    private List<PlannedSetEntity> plannedSets = new ArrayList<>();


}
