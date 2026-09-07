package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.PlanDayEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDayRepository extends CrudRepository<PlanDayEntity, Long>,
        PagingAndSortingRepository<PlanDayEntity, Long> {

    List<PlanDayEntity> findByPlanEntity_Id(Long planId);

    Optional<PlanDayEntity> findByPlanEntity_IdAndDayOfWeek(Long planId, Integer dayOfWeek);
}
