package com.lukeroche.fit.domain.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanResponse {

    private Long id;

    private String name;

    private Integer weeks;

    private Boolean active;

    private LocalDate startDate;

    private UUID createdByUserId;

    private LocalDateTime createdAt;

    private List<PlanDayResponse> days;
}
