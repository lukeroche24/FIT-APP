package com.lukeroche.fit.domain.dto.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateWorkoutExerciseRequest {

    private Long orderIndex;

    private Integer minReps;

    private Integer maxReps;
}
