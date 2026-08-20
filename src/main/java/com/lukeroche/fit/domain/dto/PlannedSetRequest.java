package com.lukeroche.fit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedSetRequest {

    private Integer targetReps;

    private Float targetWeight;

    private Integer targetDurationSeconds;

    private Integer restTimeSeconds;
}
