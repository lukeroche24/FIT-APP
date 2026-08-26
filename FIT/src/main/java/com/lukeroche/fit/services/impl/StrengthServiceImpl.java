package com.lukeroche.fit.services.impl;

import com.lukeroche.fit.domain.dto.user.StrengthExerciseOption;
import com.lukeroche.fit.domain.dto.user.StrengthStatsResponse;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.SetTracking;
import com.lukeroche.fit.domain.projections.SetHistoryRow;
import com.lukeroche.fit.repositories.ExerciseRepository;
import com.lukeroche.fit.repositories.LoggedSetRepository;
import com.lukeroche.fit.services.FriendshipService;
import com.lukeroche.fit.services.StrengthService;
import com.lukeroche.fit.services.progression.OneRepMax;
import com.lukeroche.fit.services.progression.WeakerSide;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StrengthServiceImpl implements StrengthService {

    private final FriendshipService friendshipService;
    private final ExerciseRepository exerciseRepository;
    private final LoggedSetRepository loggedSetRepository;

    @Override
    public List<StrengthExerciseOption> listTrainedExercises(UUID viewerId, UUID targetId) {
        assertCanView(viewerId, targetId);
        return exerciseRepository.findTrainedWeightedExercises(targetId).stream()
                .map(exercise -> StrengthExerciseOption.builder()
                        .id(exercise.getId())
                        .name(exercise.getName())
                        .build())
                .toList();
    }

    @Override
    public StrengthStatsResponse getStats(UUID viewerId, UUID targetId, Long exerciseId) {
        assertCanView(viewerId, targetId);
        ExerciseEntity exercise = exerciseRepository.findByIdAndCreatedByUserId(exerciseId, targetId)
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found"));
        if (!SetTracking.tracksWeight(exercise.getTracksWeight())) {
            throw new EntityNotFoundException("Exercise not found");
        }

        List<SetHistoryRow> sets = loggedSetRepository.findCompletedSetHistory(targetId, exerciseId);
        Double estimatedOneRm = null;
        Double testedOneRm = null;
        Double heaviestWeight = null;
        Integer heaviestReps = null;
        LocalDateTime heaviestAt = null;

        for (SetHistoryRow set : sets) {
            WeakerSide.Side side = successfulSide(set);
            if (side == null || side.weight() <= 0 || side.reps() <= 0) {
                continue;
            }

            double estimate = OneRepMax.epley(side.weight(), side.reps());
            if (estimatedOneRm == null || estimate > estimatedOneRm) {
                estimatedOneRm = estimate;
            }

            if (side.reps() == 1 && (testedOneRm == null || side.weight() > testedOneRm)) {
                testedOneRm = side.weight();
            }

            boolean heavier = heaviestWeight == null || side.weight() > heaviestWeight;
            boolean sameWeightMoreReps = heaviestWeight != null
                    && side.weight() == heaviestWeight
                    && side.reps() > heaviestReps;
            if (heavier || sameWeightMoreReps) {
                heaviestWeight = side.weight();
                heaviestReps = side.reps();
                heaviestAt = set.completedAt();
            }
        }

        return StrengthStatsResponse.builder()
                .exerciseId(exercise.getId())
                .exerciseName(exercise.getName())
                .estimatedOneRm(roundOne(estimatedOneRm))
                .testedOneRm(roundOne(testedOneRm))
                .heaviestWeight(roundOne(heaviestWeight))
                .heaviestReps(heaviestReps)
                .heaviestAt(heaviestAt)
                .build();
    }

    private void assertCanView(UUID viewerId, UUID targetId) {
        if (!viewerId.equals(targetId) && !friendshipService.isFriend(viewerId, targetId)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    private static WeakerSide.Side successfulSide(SetHistoryRow set) {
        boolean leftOk = set.actualReps() != null && set.actualReps() > 0
                && set.actualWeight() != null && set.actualWeight() > 0
                && !Boolean.TRUE.equals(set.failed());
        boolean rightOk = set.rightReps() != null && set.rightReps() > 0
                && set.rightWeight() != null && set.rightWeight() > 0
                && !Boolean.TRUE.equals(set.rightFailed());
        if (!leftOk && !rightOk) {
            return null;
        }
        return WeakerSide.of(
                leftOk ? set.actualReps() : null,
                leftOk ? set.actualWeight() : null,
                rightOk ? set.rightReps() : null,
                rightOk ? set.rightWeight() : null);
    }

    private static Double roundOne(Double value) {
        if (value == null || value <= 0) {
            return null;
        }
        return Math.round(value * 10.0d) / 10.0d;
    }
}
