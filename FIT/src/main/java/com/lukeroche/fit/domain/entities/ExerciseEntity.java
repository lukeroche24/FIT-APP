package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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

}
