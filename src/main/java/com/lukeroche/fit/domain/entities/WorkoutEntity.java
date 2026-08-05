package com.lukeroche.fit.domain.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private Integer createdByUserId;

    private String description;

    private String name;

    private Boolean visibility;

    @OneToMany(mappedBy = "workoutEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutExerciseEntity> workoutExercises = new ArrayList<>();

}
