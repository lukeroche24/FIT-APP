package com.lukeroche.fit;

import com.lukeroche.fit.domain.dto.ExerciseResponse;
//import com.lukeroche.fit.domain.dto.BookDto;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
//import com.lukeroche.fit.domain.entities.BookEntity;

public final class TestDataUtil {

    private TestDataUtil(){

    }

    public static ExerciseEntity createTestExerciseA() {
        return ExerciseEntity.builder()
                .name("Bench Press")
                .description("Lie flat and push barbell")
                .build();
    }

//    public static ExerciseResponse createTestExerciseDtoA() {
//        return ExerciseResponse.builder()
//                .id(1L)
//                .name("Bench Press")
//                .age(80)
//                .build();
//    }
//
//    public static ExerciseEntity createTestAuthorB() {
//        return ExerciseEntity.builder()
//                .name("Thomas Cronin")
//                .age(44)
//                .build();
//    }
//
//    public static ExerciseEntity createTestAuthorC() {
//        return ExerciseEntity.builder()
//                .name("Jesse A Casey")
//                .age(24)
//                .build();
//    }
//
//    public static BookEntity createTestBookA(final ExerciseEntity exerciseEntity) {
//        return BookEntity.builder()
//                .isbn("978-1-2345-6789-0")
//                .title("The Shadow in the Attic")
//                .exerciseEntity(exerciseEntity)
//                .build();
//    }
//
//    public static BookDto createTestBookDtoA(final ExerciseResponse author) {
//        return BookDto.builder()
//                .isbn("978-1-2345-6789-0")
//                .title("The Shadow in the Attic")
//                .author(author)
//                .build();
//    }
//
//
//    public static BookEntity createTestBookB(final ExerciseEntity exerciseEntity) {
//        return BookEntity.builder()
//                .isbn("978-1-2345-6789-1")
//                .title("The Shadow in the Attic 2")
//                .exerciseEntity(exerciseEntity)
//                .build();
//    }
//
//    public static BookEntity createTestBookC(final ExerciseEntity exerciseEntity) {
//        return BookEntity.builder()
//                .isbn("978-1-2345-6789-2")
//                .title("The Shadow in the Attic 3")
//                .exerciseEntity(exerciseEntity)
//                .build();
//    }
}
