package com.lukeroche.fit.repositories;

import com.lukeroche.fit.TestDataUtil;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ExerciseEntityRepositoryIntegrationTests {

    private ExerciseRepository underTest;

    @Autowired
    public ExerciseEntityRepositoryIntegrationTests(ExerciseRepository underTest) {
        this.underTest = underTest;
    }

    @Test
    public void testThatAuthorCanBeCreatedAndRecalled(){

        ExerciseEntity exerciseEntity = TestDataUtil.createTestAuthorA();
        underTest.save(exerciseEntity);
        Optional<ExerciseEntity> result = underTest.findById(exerciseEntity.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(exerciseEntity);
    }

    @Test
    public void testThatMultipleAuthorsCanBeCreatedAndRecalled(){
        ExerciseEntity exerciseEntityA = TestDataUtil.createTestAuthorA();
        underTest.save(exerciseEntityA);
        ExerciseEntity exerciseEntityB = TestDataUtil.createTestAuthorB();
        underTest.save(exerciseEntityB);
        ExerciseEntity exerciseEntityC = TestDataUtil.createTestAuthorC();
        underTest.save(exerciseEntityC);

        Iterable<ExerciseEntity> result = underTest.findAll();
        assertThat(result)
                .hasSize(3)
                .containsExactly(exerciseEntityA, exerciseEntityB, exerciseEntityC);
    }

    @Test
    public void testThatAuthorCanBeUpdated() {
        ExerciseEntity exerciseEntityA = TestDataUtil.createTestAuthorA();
        underTest.save(exerciseEntityA);
        exerciseEntityA.setName("UPDATED");
        underTest.save(exerciseEntityA);
        Optional<ExerciseEntity> result = underTest.findById(exerciseEntityA.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(exerciseEntityA);
    }

    @Test
    public void testThatAuthorCanBeDeleted() {
        ExerciseEntity exerciseEntityA = TestDataUtil.createTestAuthorA();
        underTest.save(exerciseEntityA);
        underTest.deleteById(exerciseEntityA.getId());
        Optional<ExerciseEntity> result = underTest.findById(exerciseEntityA.getId());
        assertThat(result).isEmpty();

    }

    @Test
    public void testThatGetAuthorsWithAgeLessThan(){
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        underTest.save(testExerciseEntityA);
        ExerciseEntity testExerciseEntityB = TestDataUtil.createTestAuthorB();
        underTest.save(testExerciseEntityB);
        ExerciseEntity testExerciseEntityC = TestDataUtil.createTestAuthorC();
        underTest.save(testExerciseEntityC);

        Iterable<ExerciseEntity> result = underTest.ageLessThan(50);
        assertThat(result).containsExactly(testExerciseEntityB, testExerciseEntityC);
    }

    @Test
    public void testThatGetAuthorsWithAgeGreaterThan(){
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        underTest.save(testExerciseEntityA);
        ExerciseEntity testExerciseEntityB = TestDataUtil.createTestAuthorB();
        underTest.save(testExerciseEntityB);
        ExerciseEntity testExerciseEntityC = TestDataUtil.createTestAuthorC();
        underTest.save(testExerciseEntityC);

        Iterable<ExerciseEntity> result = underTest.findAuthorsWithAgeGreaterThan(50);
        assertThat(result).containsExactly(testExerciseEntityA);
    }
}
