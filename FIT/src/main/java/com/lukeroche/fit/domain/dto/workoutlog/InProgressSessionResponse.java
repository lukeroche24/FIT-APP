package com.lukeroche.fit.domain.dto.workoutlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InProgressSessionResponse {

    private Long id;

    private String name;

    private LocalDateTime startedAt;
}
