package com.lukeroche.fit.domain.dto.exercise;

import com.lukeroche.fit.domain.entities.LoadingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseResponse {

    private Long id;

    private String name;

    private String description;

    private UUID createdByUserId;

    private Date createdAt;

    private LoadingType loadingType;

    private Double loadStep;

}
