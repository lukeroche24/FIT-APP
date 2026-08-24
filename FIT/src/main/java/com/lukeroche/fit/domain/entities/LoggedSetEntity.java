package com.lukeroche.fit.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "logged_sets")
public class LoggedSetEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "logged_exercise_id")
    private LoggedExerciseEntity loggedExerciseEntity;

    private Integer setNumber;

    private Integer actualReps;

    private Float actualWeight;

    //private Integer actualDurationSeconds;

    //private Float actualDistance;

    private String notes;

    private LocalDateTime loggedAt;

}
