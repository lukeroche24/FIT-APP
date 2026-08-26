package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import org.springframework.stereotype.Component;

@Component
public class LoadingSchemeFactory {

    public LoadingScheme forExercise(ExerciseEntity exercise) {
        if (exercise.getLoadingType() == LoadingType.BODYWEIGHT) {
            return new BodyweightLoading();
        }

        Double loadStep = exercise.getLoadStep();
        if (loadStep == null || loadStep <= 0) {
            throw new IllegalStateException(
                    "Exercise is missing a load step. Set one on the exercise, or leave it blank to use the default.");
        }
        return new StepLoading(loadStep);
    }
}
