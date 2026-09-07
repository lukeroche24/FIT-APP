package com.lukeroche.fit.services;

import com.lukeroche.fit.domain.dto.user.StrengthExerciseOption;
import com.lukeroche.fit.domain.dto.user.StrengthStatsResponse;

import java.util.List;
import java.util.UUID;

/**
 * Strength page numbers for a user (self or a friend). Estimated 1RM is the
 * best Epley in a recent window; tested 1RM and heaviest lift are all-time.
 * Failed sides are ignored.
 */
public interface StrengthService {

    List<StrengthExerciseOption> listTrainedExercises(UUID viewerId, UUID targetId);

    /**
     * Estimated 1RM: best Epley in {@code fit.strength.estimated-one-rm-days}.
     * Tested 1RM and heaviest successful set: all-time.
     */
    StrengthStatsResponse getStats(UUID viewerId, UUID targetId, Long exerciseId);
}
