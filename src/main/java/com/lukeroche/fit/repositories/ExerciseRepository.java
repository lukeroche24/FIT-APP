package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends CrudRepository<ExerciseEntity, Long>,
        PagingAndSortingRepository<ExerciseEntity, Long> {

    Page<ExerciseEntity> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Optional<ExerciseEntity> findByIdAndCreatedByUserId(Long id, UUID createdByUserId);

    boolean existsByIdAndCreatedByUserId(Long id, UUID createdByUserId);
}
