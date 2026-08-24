package com.lukeroche.fit.domain.dto.workoutlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutLogRequest {

    private String name;

    private String notes;
}
