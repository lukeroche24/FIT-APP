package com.lukeroche.fit.domain.dto.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddWorkoutExerciseRequest {

    private Long exerciseId;

    private Integer minReps;

    private Integer maxReps;

}
