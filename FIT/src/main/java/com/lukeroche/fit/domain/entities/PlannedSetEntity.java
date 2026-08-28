package com.lukeroche.fit.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "planned_sets")
public class PlannedSetEntity extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "workout_exercise_id")
    private WorkoutExerciseEntity workoutExerciseEntity;

    private Integer setNumber;

    private Integer targetReps;

    private Float targetWeight;

    private Integer rightReps;

    private Float rightWeight;

    private Integer targetDurationSeconds;

    private Float targetDistance;

    private Integer restTimeSeconds;

}
