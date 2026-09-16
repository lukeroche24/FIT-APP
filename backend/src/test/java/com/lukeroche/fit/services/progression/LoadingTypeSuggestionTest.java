/*
 * Filename: LoadingTypeSuggestionTest.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoadingTypeSuggestionTest {

    private final LoadingTypeSuggestion suggestion = new LoadingTypeSuggestion();

    @Test
    void infersTypeFromTheName() {
        assertEquals(LoadingType.BODYWEIGHT, suggestion.suggest("Pull-up"));
        assertEquals(LoadingType.DUMBBELL, suggestion.suggest("Dumbbell row"));
        assertEquals(LoadingType.MACHINE, suggestion.suggest("Cable fly"));
        assertEquals(LoadingType.BARBELL, suggestion.suggest("Bench press"));
        assertEquals(LoadingType.BARBELL, suggestion.suggest(null));
    }

    @Test
    void applyDefaultsFillsTypeAndStepWhenBlank() {
        ExerciseEntity barbell = ExerciseEntity.builder().name("Squat").build();
        suggestion.applyDefaults(barbell);
        assertEquals(LoadingType.BARBELL, barbell.getLoadingType());
        assertEquals(2.5, barbell.getLoadStep());

        ExerciseEntity dumbbell = ExerciseEntity.builder().name("Dumbbell press").build();
        suggestion.applyDefaults(dumbbell);
        assertEquals(LoadingType.DUMBBELL, dumbbell.getLoadingType());
        assertEquals(2.0, dumbbell.getLoadStep());

        ExerciseEntity machine = ExerciseEntity.builder().name("Leg press").build();
        suggestion.applyDefaults(machine);
        assertEquals(LoadingType.MACHINE, machine.getLoadingType());
        assertEquals(5.0, machine.getLoadStep());
    }

    @Test
    void unweightedBodyweightDoesNotGetALoadStep() {
        ExerciseEntity pullUp = ExerciseEntity.builder()
                .name("Pull-up")
                .tracksWeight(false)
                .build();
        suggestion.applyDefaults(pullUp);
        assertEquals(LoadingType.BODYWEIGHT, pullUp.getLoadingType());
        assertNull(pullUp.getLoadStep());
    }

    @Test
    void loadedBodyweightGetsTwoPointFiveKgSteps() {
        ExerciseEntity dip = ExerciseEntity.builder()
                .name("Dip")
                .tracksWeight(true)
                .build();
        suggestion.applyDefaults(dip);
        assertEquals(LoadingType.BODYWEIGHT, dip.getLoadingType());
        assertEquals(2.5, dip.getLoadStep());
    }

    @Test
    void doesNotOverwriteAnExistingTypeOrStep() {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .name("Cable row")
                .loadingType(LoadingType.BARBELL)
                .loadStep(1.25)
                .build();
        suggestion.applyDefaults(exercise);
        assertEquals(LoadingType.BARBELL, exercise.getLoadingType());
        assertEquals(1.25, exercise.getLoadStep());
    }
}
