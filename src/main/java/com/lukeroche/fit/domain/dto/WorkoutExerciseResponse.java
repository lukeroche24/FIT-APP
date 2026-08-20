package com.lukeroche.fit.domain.dto;


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

    private List<PlannedSetResponse> plannedSets;

}
