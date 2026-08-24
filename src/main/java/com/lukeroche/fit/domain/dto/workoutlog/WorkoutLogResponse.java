package com.lukeroche.fit.domain.dto.workoutlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutLogResponse {

    private Long id;

    private String name;

    private String notes;

    private UUID createdByUserId;

    private Long sourceWorkoutId;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private List<LoggedExerciseResponse> loggedExercises;
}
