package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = {"loggedExercises"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "workout_logs", indexes = {
        @Index(name = "idx_workout_logs_user_completed", columnList = "created_by_user_id, completed_at")
})
public class WorkoutLogEntity extends BaseEntity {

    private UUID createdByUserId;

    private Long sourceWorkoutId;

    private String name;

    private String notes;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "workoutLogEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoggedExerciseEntity> loggedExercises = new ArrayList<>();

}
