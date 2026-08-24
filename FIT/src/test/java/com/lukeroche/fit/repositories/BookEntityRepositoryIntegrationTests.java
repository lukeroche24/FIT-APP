//package com.lukeroche.fit.repositories;
//
//import com.lukeroche.fit.TestDataUtil;
//import com.lukeroche.fit.domain.entities.ExerciseEntity;
//import com.lukeroche.fit.domain.entities.BookEntity;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ExtendWith(SpringExtension.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class BookEntityRepositoryIntegrationTests {
//
//    private BookRepository underTest;
//
//    @Autowired
//    public BookEntityRepositoryIntegrationTests(BookRepository underTest) {
//        this.underTest = underTest;
//    }
//
//    @Test
//    public void testThatBookCanBeCreatedAndRecalled(){
//        ExerciseEntity exerciseEntity = TestDataUtil.createTestAuthorA();
//        BookEntity bookEntity = TestDataUtil.createTestBookA(exerciseEntity);
//        bookEntity = underTest.save(bookEntity);
//        Optional<BookEntity> result = underTest.findById(bookEntity.getIsbn());
//        assertThat(result).isPresent();
//        assertThat(result.get()).isEqualTo(bookEntity);
//    }
//
//    @Test
//    public void testThatMultipleBooksCanBeCreatedAndRecalled(){
//        ExerciseEntity exerciseEntity = TestDataUtil.createTestAuthorA();
//
//        BookEntity bookEntityA = TestDataUtil.createTestBookA(exerciseEntity);
//
//        bookEntityA = underTest.save(bookEntityA);
//        BookEntity bookEntityB = TestDataUtil.createTestBookB(exerciseEntity);
//
//        bookEntityB = underTest.save(bookEntityB);
//        BookEntity bookEntityC = TestDataUtil.createTestBookC(exerciseEntity);
//
//        bookEntityC = underTest.save(bookEntityC);
//
//        Iterable<BookEntity> result = underTest.findAll();
//        assertThat(result)
//                .hasSize(3)
//                .containsExactly(bookEntityA, bookEntityB, bookEntityC);
//    }
//
//    @Test
//    public void testThatBookCanBeUpdated(){
//        ExerciseEntity exerciseEntity = TestDataUtil.createTestAuthorA();
//
//        BookEntity bookEntityA = TestDataUtil.createTestBookA(exerciseEntity);
//        underTest.save(bookEntityA);
//
//        bookEntityA.setTitle("UPDATED");
//        bookEntityA = underTest.save(bookEntityA);
//
//        Optional<BookEntity> result = underTest.findById(bookEntityA.getIsbn());
//        assertThat(result).isPresent();
//        assertThat(result.get()).isEqualTo(bookEntityA);
//
//    }
//
//    @Test
//    public void testThatBookCanBeDeleted() {
//        ExerciseEntity exerciseEntity = TestDataUtil.createTestAuthorA();
//
//        BookEntity bookEntityA = TestDataUtil.createTestBookA(exerciseEntity);
//        underTest.save(bookEntityA);
//        underTest.deleteById(bookEntityA.getIsbn());
//        Optional<BookEntity> result = underTest.findById(bookEntityA.getIsbn());
//        assertThat(result).isEmpty();
//    }
//}