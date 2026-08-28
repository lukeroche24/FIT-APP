package com.lukeroche.fit.domain.dto.workout;

import com.lukeroche.fit.domain.dto.exercise.ExerciseResponse;
import com.lukeroche.fit.domain.entities.LimbPattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutExerciseResponse {

    private Long id;

    private Long orderIndex;

    private String notes;

    private ExerciseResponse exercise;

    private Integer minReps;

    private Integer maxReps;

    private Boolean tracksWeight;

    private Boolean tracksDuration;

    private Boolean tracksDistance;

    private LimbPattern limbPattern;

    private Boolean independentLoads;

    private List<PlannedSetResponse> plannedSets;

}
