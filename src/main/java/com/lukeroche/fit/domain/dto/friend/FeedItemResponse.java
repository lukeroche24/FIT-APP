package com.lukeroche.fit.domain.dto.friend;

import com.lukeroche.fit.domain.dto.workoutlog.WorkoutLogResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedItemResponse {

    private WorkoutLogResponse workoutLog;

    private UUID friendUserId;

    private String friendUsername;

    private String friendName;
}
