package com.lukeroche.fit.domain.dto.workoutlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoggedSetRequest {

    private Integer actualReps;

    private Float actualWeight;

    private Integer rightReps;

    private Float rightWeight;

    private Integer actualDurationSeconds;

    private Float actualDistance;

    private String notes;

    private Boolean failed;

    private Boolean rightFailed;
}
