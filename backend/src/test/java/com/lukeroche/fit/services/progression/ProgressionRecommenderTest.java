package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.services.StrengthConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProgressionRecommenderTest {

    private final ProgressionRecommender recommender =
            new ProgressionRecommender(new ProgressionConfig(5, 0.25, 3, 0.9, 14, 28), new StrengthConfig(28));
    private final LoadingScheme scheme = new StepLoading(2.5);
    private final ExerciseEntity barbell = ExerciseEntity.builder()
            .name("Bench press")
            .loadingType(LoadingType.BARBELL)
            .build();
    private final ExerciseEntity bodyweight = ExerciseEntity.builder()
            .name("Pull-up")
            .loadingType(LoadingType.BODYWEIGHT)
            .tracksWeight(false)
            .build();

    @Test
    void holdsWhenAnySetMissedTarget() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, false, 0)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(8, rec.targetReps());
        assertEquals(80.0, rec.targetWeight());
    }

    @Test
    void doesNotDeloadFromAFailedSetEvenIfOneRmTrendIsDown() {
        Recommendation rec = recommender.recommend(
                ProgressionState.REGRESSING,
                List.of(session(8, 80, false, 0)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(8, rec.targetReps());
        assertEquals(80.0, rec.targetWeight());
    }

    @Test
    void addsARepWhenEverySetHit() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, true, 0)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(9, rec.targetReps());
        assertEquals(80.0, rec.targetWeight());
    }

    @Test
    void convertsFromTheActualOneRmNotTheSeededTarget() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(1, 105, true, 0)),
                barbell,
                scheme,
                3,
                3);

        assertEquals(3, rec.targetReps());
        assertEquals(scheme.nearest(OneRepMax.weightForReps(105, 3)), rec.targetWeight());
    }

    @Test
    void stillDeloadsAfterALongLayoffEvenIfLastSessionMissed() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, false, 30)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(ProgressionState.REGRESSING, rec.state());
        assertEquals(8, rec.targetReps());
        assertEquals(72.5, rec.targetWeight());
    }

    @Test
    void holdsBodyweightRepsWhenPrescriptionMissed() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 0, false, 0)),
                bodyweight,
                scheme,
                6,
                12);

        assertEquals(8, rec.targetReps());
        assertNull(rec.targetWeight());
    }

    @Test
    void holdsWeightAfterATwoWeekLayoffEvenIfLastSessionHit() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, true, 16)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(8, rec.targetReps());
        assertEquals(80.0, rec.targetWeight());
    }

    @Test
    void convertsOneRmIntoThreeRmWithoutAddingAStep() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(1, 100, true, 0)),
                barbell,
                scheme,
                3,
                3);

        double expected = scheme.nearest(OneRepMax.weightForReps(100, 3));
        assertEquals(3, rec.targetReps());
        assertEquals(expected, rec.targetWeight());
    }

    @Test
    void convertsIntoANewRangeEvenIfLastSessionMissed() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(1, 100, false, 0)),
                barbell,
                scheme,
                3,
                3);

        assertEquals(3, rec.targetReps());
        assertEquals(scheme.nearest(OneRepMax.weightForReps(100, 3)), rec.targetWeight());
    }

    @Test
    void usesPredictedOneRmWhenConvertingFromARecentHeavierEstimate() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, true, 5), session(1, 90, true, 0)),
                barbell,
                scheme,
                3,
                3);

        double predicted = Math.max(OneRepMax.epley(80, 8), OneRepMax.epley(90, 1));
        assertEquals(3, rec.targetReps());
        assertEquals(scheme.nearest(OneRepMax.weightForReps(predicted, 3)), rec.targetWeight());
    }

    @Test
    void deloadsTheConvertedLoadAfterALongLayoffInANewRange() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(1, 100, true, 30)),
                barbell,
                scheme,
                3,
                3);

        double converted = OneRepMax.weightForReps(100, 3) * 0.9;
        assertEquals(ProgressionState.REGRESSING, rec.state());
        assertEquals(3, rec.targetReps());
        assertEquals(scheme.nearest(converted), rec.targetWeight());
    }

    @Test
    void extraRepsAfterAHitStayOnDoubleProgression() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(9, 80, true, 0)),
                barbell,
                scheme,
                8,
                8);

        assertEquals(8, rec.targetReps());
        assertEquals(82.5, rec.targetWeight());
    }

    @Test
    void doubleProgressionAddsLoadAtTheTopOfTheRange() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(12, 80, true, 0)),
                barbell,
                scheme,
                6,
                12);

        assertEquals(6, rec.targetReps());
        assertEquals(82.5, rec.targetWeight());
    }

    @Test
    void loadedBodyweightAddsARepWhenEverySetHit() {
        ExerciseEntity dip = ExerciseEntity.builder()
                .name("Dip")
                .loadingType(LoadingType.BODYWEIGHT)
                .tracksWeight(true)
                .loadStep(2.5)
                .build();

        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 5, true, 0)),
                dip,
                new StepLoading(2.5),
                6,
                12);

        assertEquals(9, rec.targetReps());
        assertEquals(5.0, rec.targetWeight());
    }

    @Test
    void swapsAnInvertedRepRange() {
        Recommendation rec = recommender.recommend(
                ProgressionState.PROGRESSING,
                List.of(session(8, 80, true, 0)),
                barbell,
                scheme,
                12,
                6);

        assertEquals(9, rec.targetReps());
        assertEquals(80.0, rec.targetWeight());
    }

    @Test
    void emptyHistoryHasNoTargets() {
        Recommendation rec = recommender.recommend(
                ProgressionState.NEW,
                List.of(),
                barbell,
                scheme,
                6,
                12);

        assertNull(rec.targetReps());
        assertNull(rec.targetWeight());
    }

    private static SessionStrength session(int reps, double weight, boolean hit, int daysAgo) {
        return new SessionStrength(
                1L,
                LocalDateTime.now().minusDays(daysAgo),
                100,
                reps,
                weight,
                hit);
    }
}
