/*
 * Filename: ExerciseEntity.java
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Private exercise in the owner's library. Loading type and load step drive
 * how progression snaps weight; laterality and tracking flags drive the UI.
 */
@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "exercises")
public class ExerciseEntity extends BaseEntity{

    private String name;

    private String description;

    private UUID createdByUserId;

    @Enumerated(EnumType.STRING)
    private LoadingType loadingType;

    private Double loadStep;

    @Builder.Default
    private Boolean tracksWeight = true;

    @Builder.Default
    private Boolean tracksDuration = false;

    @Builder.Default
    private Boolean tracksDistance = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LimbPattern limbPattern = LimbPattern.BILATERAL;

    @Builder.Default
    private Boolean independentLoads = false;

}
