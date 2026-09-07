package com.lukeroche.fit.domain.dto.plan;

import com.lukeroche.fit.domain.dto.workout.WorkoutResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpcomingWorkoutResponse {

    private LocalDate date;

    private Integer dayOfWeek;

    private Integer weekNumber;

    private WorkoutResponse workout;

    private PlanOccurrenceStatus status;

    private Long workoutLogId;
}
