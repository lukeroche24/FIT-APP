package com.lukeroche.fit.domain.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedItemResponse {

    private Long workoutLogId;

    private String workoutName;

    private LocalDateTime completedAt;

    private List<String> exerciseNames;

    private UUID friendUserId;

    private String friendUsername;

    private String friendName;
}
