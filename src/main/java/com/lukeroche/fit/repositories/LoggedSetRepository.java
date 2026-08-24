package com.lukeroche.fit.repositories;

import com.lukeroche.fit.domain.entities.LoggedSetEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoggedSetRepository extends CrudRepository<LoggedSetEntity, Long>,
        PagingAndSortingRepository<LoggedSetEntity, Long> {

    boolean existsByIdAndLoggedExerciseEntity_Id(Long id, Long loggedExerciseId);

    long countByLoggedExerciseEntity_Id(Long loggedExerciseId);

    List<LoggedSetEntity> findByLoggedExerciseEntity_IdOrderBySetNumberAsc(Long loggedExerciseId);
}
