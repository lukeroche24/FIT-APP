package com.lukeroche.fit.domain.dto.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutResponse {
    private Long id;

    private String name;

    private String description;

    private UUID createdByUserId;

    private Date createdAt;

    private Boolean visibility;

    private List<WorkoutExerciseResponse> exercises;
}
