package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.SetTracking;
import org.springframework.stereotype.Component;

@Component
public class LoadingSchemeFactory {

    private static final double BODYWEIGHT_LOAD_STEP = 2.5;

    public LoadingScheme forExercise(ExerciseEntity exercise) {
        if (exercise.getLoadingType() == LoadingType.BODYWEIGHT
                && !SetTracking.tracksWeight(exercise.getTracksWeight())) {
            return new BodyweightLoading();
        }

        Double loadStep = exercise.getLoadStep();
        if (loadStep == null || loadStep <= 0) {
            if (exercise.getLoadingType() == LoadingType.BODYWEIGHT) {
                loadStep = BODYWEIGHT_LOAD_STEP;
            } else {
                throw new IllegalStateException(
                        "Exercise is missing a load step. Set one on the exercise, or leave it blank to use the default.");
            }
        }
        return new StepLoading(loadStep);
    }

    public Float snapWeight(ExerciseEntity exercise, Float weight) {
        if (weight == null || exercise == null) {
            return weight;
        }
        try {
            return (float) forExercise(exercise).snap(weight.doubleValue());
        } catch (RuntimeException e) {
            return weight;
        }
    }
}
