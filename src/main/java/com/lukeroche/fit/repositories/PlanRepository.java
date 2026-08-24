package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.PlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends CrudRepository<PlanEntity, Long>,
        PagingAndSortingRepository<PlanEntity, Long> {

    Page<PlanEntity> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Optional<PlanEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    Optional<PlanEntity> findByCreatedByUserIdAndActiveTrue(UUID createdByUserId);
}
