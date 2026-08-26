package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.user.StrengthExerciseOption;
import com.lukeroche.fit.domain.dto.user.StrengthStatsResponse;

import java.util.List;
import java.util.UUID;

public interface StrengthService {

    List<StrengthExerciseOption> listTrainedExercises(UUID viewerId, UUID targetId);

    StrengthStatsResponse getStats(UUID viewerId, UUID targetId, Long exerciseId);
}
