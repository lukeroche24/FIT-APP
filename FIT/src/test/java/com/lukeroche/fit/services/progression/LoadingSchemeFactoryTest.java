package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoadingSchemeFactoryTest {

    private final LoadingSchemeFactory factory = new LoadingSchemeFactory();

    @Test
    void unweightedBodyweightHasNoLoadScheme() {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .loadingType(LoadingType.BODYWEIGHT)
                .tracksWeight(false)
                .build();

        assertInstanceOf(BodyweightLoading.class, factory.forExercise(exercise));
    }

    @Test
    void loadedBodyweightDefaultsToTwoPointFiveKgSteps() {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .loadingType(LoadingType.BODYWEIGHT)
                .tracksWeight(true)
                .build();

        LoadingScheme scheme = factory.forExercise(exercise);
        assertInstanceOf(StepLoading.class, scheme);
        assertEquals(2.5, scheme.snap(2.4));
    }

    @Test
    void barbellUsesTheStoredStep() {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .loadingType(LoadingType.BARBELL)
                .loadStep(2.5)
                .build();

        assertEquals(82.5, factory.forExercise(exercise).snap(81.4));
    }

    @Test
    void weightedExerciseWithoutAStepIsRejected() {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .loadingType(LoadingType.BARBELL)
                .build();

        assertThrows(IllegalStateException.class, () -> factory.forExercise(exercise));
    }

    @Test
    void snapWeightLeavesNullsAndBrokenSchemesAlone() {
        assertEquals(80f, factory.snapWeight(null, 80f));
        assertNull(factory.snapWeight(ExerciseEntity.builder().build(), null));

        ExerciseEntity missingStep = ExerciseEntity.builder()
                .loadingType(LoadingType.BARBELL)
                .build();
        assertEquals(80f, factory.snapWeight(missingStep, 80f));
    }
}
