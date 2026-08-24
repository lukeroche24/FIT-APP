package com.lukeroche.fit.domain.dto.workoutlog;

import com.lukeroche.fit.domain.dto.exercise.ExerciseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoggedExerciseResponse {

    private Long id;

    private Long orderIndex;

    private String notes;

    private ExerciseResponse exercise;

    private List<LoggedSetResponse> loggedSets;
}
