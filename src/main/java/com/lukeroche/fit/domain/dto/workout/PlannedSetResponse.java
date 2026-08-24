package com.lukeroche.fit.domain.dto.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedSetResponse {

    private Long id;

    private Integer setNumber;

    private Integer targetReps;

    private Float targetWeight;

    private Integer targetDurationSeconds;

    private Integer restTimeSeconds;
}
