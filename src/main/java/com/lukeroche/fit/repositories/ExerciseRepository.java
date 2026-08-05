package com.lukeroche.fit.repositories;


import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRepository extends CrudRepository<ExerciseEntity, Long>,
        PagingAndSortingRepository<ExerciseEntity, Long> {
    }
//    Iterable<ExerciseEntity> ageLessThan(int age);
//
//    @Query("SELECT a FROM ExerciseEntity a where a.age > ?1")
//    Iterable<ExerciseEntity> findAuthorsWithAgeGreaterThan(int age);

