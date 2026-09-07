package com.lukeroche.fit.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StrengthStatsResponse {

    private Long exerciseId;

    private String exerciseName;

    private Double estimatedOneRm;

    private Double testedOneRm;

    private Double heaviestWeight;

    private Integer heaviestReps;

    private LocalDateTime heaviestAt;
}
