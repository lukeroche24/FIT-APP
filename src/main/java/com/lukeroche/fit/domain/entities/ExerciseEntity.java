package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;


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

    private Integer createdByUserId;

}
