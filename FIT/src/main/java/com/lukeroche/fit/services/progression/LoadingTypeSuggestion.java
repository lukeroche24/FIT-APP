package com.lukeroche.fit.services.progression;

import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.domain.entities.LoadingType;
import com.lukeroche.fit.domain.entities.SetTracking;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoadingTypeSuggestion {

    private static final List<String> BODYWEIGHT_HINTS = List.of(
            "pull up", "pull-up", "pullup", "chin up", "chin-up",
            "push up", "push-up", "pushup", "dip", "plank", "crunch", "sit up"
    );

    private static final List<String> DUMBBELL_HINTS = List.of("dumbbell");

    private static final List<String> MACHINE_HINTS = List.of(
            "cable", "machine", "pulldown", "pushdown", "pec deck",
            "leg press", "leg extension", "leg curl", "seated row"
    );

    public LoadingType suggest(String exerciseName) {

        String name = exerciseName == null ? "" : exerciseName.toLowerCase();

        if(matchesAny(name, BODYWEIGHT_HINTS)) return LoadingType.BODYWEIGHT;
        if(matchesAny(name, DUMBBELL_HINTS)) return LoadingType.DUMBBELL;
        if(matchesAny(name, MACHINE_HINTS)) return LoadingType.MACHINE;
        return LoadingType.BARBELL;
    }

    public void applyDefaults(ExerciseEntity exerciseEntity) {
        if(exerciseEntity.getLoadingType() == null){
            exerciseEntity.setLoadingType(suggest(exerciseEntity.getName()));
        }

        if (exerciseEntity.getLoadStep() == null
                || exerciseEntity.getLoadStep() <= 0) {
            if (exerciseEntity.getLoadingType() == LoadingType.BODYWEIGHT) {
                if (SetTracking.tracksWeight(exerciseEntity.getTracksWeight())) {
                    exerciseEntity.setLoadStep(2.5);
                }
            } else if (exerciseEntity.getLoadingType() == LoadingType.DUMBBELL) {
                exerciseEntity.setLoadStep(2.0);
            } else if (exerciseEntity.getLoadingType() == LoadingType.MACHINE) {
                exerciseEntity.setLoadStep(5.0);
            } else {
                exerciseEntity.setLoadStep(2.5);
            }
        }
    }

    private boolean matchesAny(String name, List<String> hints) {
        return hints.stream().anyMatch(name::contains);
    }

}
