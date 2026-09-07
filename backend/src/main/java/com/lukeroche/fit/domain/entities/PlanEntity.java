package com.lukeroche.fit.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Weekly plan. At most one row per user has {@code active == true};
 * {@code startDate} is set when it is activated.
 */
@Getter
@Setter
@ToString(exclude = {"planDays"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "plans")
public class PlanEntity extends BaseEntity {

    private UUID createdByUserId;

    private String name;

    private Integer weeks;

    private Boolean active;

    private LocalDate startDate;

    @OneToMany(mappedBy = "planEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanDayEntity> planDays = new ArrayList<>();

}
