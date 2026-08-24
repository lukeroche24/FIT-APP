package com.lukeroche.fit.domain.dto.plan;

import com.lukeroche.fit.domain.dto.workout.WorkoutResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanDayResponse {

    private Long id;

    private Integer dayOfWeek;

    private WorkoutResponse workout;
}
