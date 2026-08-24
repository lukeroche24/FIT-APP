package com.lukeroche.fit.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "plan_days")
public class PlanDayEntity extends BaseEntity {

    private Integer dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PlanEntity planEntity;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private WorkoutEntity workoutEntity;

}
