package com.lukeroche.fit.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeResponse {

    private UUID id;

    private String name;

    private String username;

    private String email;

    private LocalDateTime createdAt;

    private long completedSessionCount;

    private long workoutCount;

    private String activePlanName;
}
