package com.lukeroche.fit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkoutResponse {
    private Long id;

    private String name;

    private String description;

    private Integer createdByUserId;

    private Date createdAt;

    private Boolean visibility;
}
