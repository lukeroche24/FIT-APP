package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private Boolean visibility;

    @OneToMany(mappedBy = "workoutEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutExerciseEntity> workoutExercises = new ArrayList<>();

}
