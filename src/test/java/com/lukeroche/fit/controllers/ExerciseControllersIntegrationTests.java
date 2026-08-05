package com.lukeroche.fit.controllers;

import com.lukeroche.fit.TestDataUtil;
import com.lukeroche.fit.domain.dto.ExerciseResponse;
import com.lukeroche.fit.domain.entities.ExerciseEntity;
import com.lukeroche.fit.services.ExerciseService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ExerciseControllersIntegrationTests {

    private ExerciseService exerciseService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    public ExerciseControllersIntegrationTests(MockMvc mockMvc, ExerciseService exerciseService) {
        this.mockMvc = mockMvc;
        this.exerciseService = exerciseService;
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testThatCreateAuthorSuccessfullyReturnsHttp201Created() throws Exception{
        ExerciseEntity testAuthorA = TestDataUtil.createTestAuthorA();
        testAuthorA.setId(null);
        String authorJson = objectMapper.writeValueAsString(testAuthorA);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/authors")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorJson)
        ). andExpect(
                MockMvcResultMatchers.status().isCreated()
        );
    }

    @Test
    public void testThatCreateAuthorSuccessfullyReturnsSavedAuthor() throws Exception{
        ExerciseEntity testAuthorA = TestDataUtil.createTestAuthorA();
        testAuthorA.setId(null);
        String authorJson = objectMapper.writeValueAsString(testAuthorA);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/authors")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorJson)
        ). andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("Abigail Rose")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.age").value("80")
        );
    }

    @Test
    public void testThatListAuthorsReturnsHttpStatus200() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/authors")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void testThatListAuthorsReturnsListOfAuthors() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        exerciseService.save(testExerciseEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authors")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.jsonPath("$.[0]id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.[0]name").value("Abigail Rose")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.[0]age").value("80")
        );

    }

    @Test
    public void testThatGetAuthorsReturnsHttpStatus200WhenAuthorExists() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        exerciseService.save(testExerciseEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authors/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void testThatGetAuthorsReturnsHttpStatus404WhenAuthorDoesntExists() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/authors/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void testThatGetAuthorsReturnsAuthorWhenAuthorExists() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        exerciseService.save(testExerciseEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/authors/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(1)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("Abigail Rose")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.age").value("80")
        );

    }

    @Test
    public void testThatFullUpdateAuthorReturnsHttpStatus404WhenNoAuthorExists() throws Exception {
        ExerciseResponse testExerciseResponseA = TestDataUtil.createTestAuthorDtoA();
        String authorDtoJson = objectMapper.writeValueAsString(testExerciseResponseA);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/authors/99")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatFullUpdateAuthorReturnsHttpStatus200WhenAuthorExists() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        ExerciseEntity savedAuthor = exerciseService.save(testExerciseEntityA);

        ExerciseResponse testExerciseResponseA = TestDataUtil.createTestAuthorDtoA();
        String authorDtoJson = objectMapper.writeValueAsString(testExerciseResponseA);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/authors/" + savedAuthor.getId())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatFullUpdateUpdatesExistingAuthor() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        ExerciseEntity savedAuthor = exerciseService.save(testExerciseEntityA);

        ExerciseEntity authorDto = TestDataUtil.createTestAuthorB();
        authorDto.setId(savedAuthor.getId());

        String authorDtoUpdateJson = objectMapper.writeValueAsString(authorDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/authors/" + savedAuthor.getId())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorDtoUpdateJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedAuthor.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(authorDto.getName())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.age").value(authorDto.getAge())
        );
    }

    @Test
    public void testThatPartialUpdateExistingAuthorReturnsHttpStatus20Ok() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        ExerciseEntity savedAuthor = exerciseService.save(testExerciseEntityA);

        ExerciseResponse testExerciseResponseA = TestDataUtil.createTestAuthorDtoA();
        testExerciseResponseA.setName("UPDATED");
        String authorDtoJson = objectMapper.writeValueAsString(testExerciseResponseA);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/authors/" + savedAuthor.getId())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatPartialUpdateExistingAuthorReturnsUpdatedAuthor() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        ExerciseEntity savedAuthor = exerciseService.save(testExerciseEntityA);

        ExerciseResponse testExerciseResponseA = TestDataUtil.createTestAuthorDtoA();
        testExerciseResponseA.setName("UPDATED");
        String authorDtoJson = objectMapper.writeValueAsString(testExerciseResponseA);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/authors/" + savedAuthor.getId())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(authorDtoJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedAuthor.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("UPDATED")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.age").value(testExerciseResponseA.getAge())
        );
    }

    @Test
    public void testThatDeleteAuthorReturnsHttpStatus204ForNonExistingAuthor() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/authors/999")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteAuthorReturnsHttpStatus204ForExistingAuthor() throws Exception {
        ExerciseEntity testExerciseEntityA = TestDataUtil.createTestAuthorA();
        ExerciseEntity savedAuthor = exerciseService.save(testExerciseEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/authors/" + savedAuthor.getId())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
