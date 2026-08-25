package com.lukeroche.fit.domain.dto.exercise;

import com.lukeroche.fit.domain.entities.LoadingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseRequest {

    private String name;

    private String description;

    private LoadingType loadingType;

    private Double loadStep;
}
