package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
