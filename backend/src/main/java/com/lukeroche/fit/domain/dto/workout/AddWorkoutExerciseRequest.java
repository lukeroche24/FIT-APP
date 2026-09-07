package com.lukeroche.fit.domain.dto.workout;

import com.lukeroche.fit.domain.entities.LimbPattern;
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

    private Boolean tracksWeight;

    private Boolean tracksDuration;

    private Boolean tracksDistance;

    private LimbPattern limbPattern;

    private Boolean independentLoads;

}
