package com.lukeroche.fit.controllers;

import com.lukeroche.fit.TestDataUtil;
import com.lukeroche.fit.domain.dto.BookDto;
import com.lukeroche.fit.domain.entities.BookEntity;
import com.lukeroche.fit.services.BookService;
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
public class BookControllerIntegrationTests {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private BookService bookService;

    @Autowired
    public BookControllerIntegrationTests(MockMvc mockMvc, BookService bookService) {
        this.mockMvc = mockMvc;
        this.bookService = bookService;
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testThatCreateBookReturnsHttpStatus201Created() throws Exception{

        BookDto bookDto = TestDataUtil.createTestBookDtoA(null);
        String createBookJson = objectMapper.writeValueAsString(bookDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/books/" +bookDto.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(createBookJson)
        ). andExpect(
                MockMvcResultMatchers.status().isCreated()
        );

    }

    @Test
    public void testThatUpdateBookReturnsHttpStatus200Ok() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        BookEntity savedBookEntity = bookService.createUpdateBook(
                testBookEntityA.getIsbn(), testBookEntityA
        );

        BookDto testBookA = TestDataUtil.createTestBookDtoA(null);
        testBookA.setIsbn(savedBookEntity.getIsbn());
        String bookJson = objectMapper.writeValueAsString(testBookA);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/books/" + savedBookEntity.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(bookJson)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatCreateBookReturnsCreatedBook() throws Exception{

        BookDto bookDto = TestDataUtil.createTestBookDtoA(null);
        String createBookJson = objectMapper.writeValueAsString(bookDto);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/books/" +bookDto.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(createBookJson)
        ). andExpect(
                MockMvcResultMatchers.jsonPath("$.isbn").value(bookDto.getIsbn())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value(bookDto.getTitle())
        );

    }

    @Test
    public void testThatUpdateBookReturnsUpdatedBook() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        BookEntity savedBookEntity = bookService.createUpdateBook(
                testBookEntityA.getIsbn(), testBookEntityA
        );

        BookDto testBookA = TestDataUtil.createTestBookDtoA(null);
        testBookA.setIsbn(savedBookEntity.getIsbn());
        testBookA.setTitle("UPDATED");
        String bookJson = objectMapper.writeValueAsString(testBookA);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/books/" + savedBookEntity.getIsbn() )
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(bookJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.isbn").value("978-1-2345-6789-0")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("UPDATED")
        );
    }

    @Test
    public void testThatListBooksReturnsHttpStatus200Created() throws Exception{
        mockMvc.perform(
                MockMvcRequestBuilders.get("/books")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.status().isOk()
        );

    }

    @Test
    public void testThatListBooksReturnsBook() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        bookService.createUpdateBook(testBookEntityA.getIsbn(), testBookEntityA);
        mockMvc.perform(
                MockMvcRequestBuilders.get("/books")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.jsonPath("$[0].isbn").value("978-1-2345-6789-0")
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].title").value("The Shadow in the Attic")
        );

    }

    @Test
    public void testThatGetBookReturnsHttpStatus200OkWhenBookExists() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        bookService.createUpdateBook(testBookEntityA.getIsbn(), testBookEntityA);
        mockMvc.perform(
                MockMvcRequestBuilders.get("/books/" +testBookEntityA.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.status().isOk()
        );

    }

    @Test
    public void testThatGetBookReturnsHttpStatus404NotFoundWhenBookDoesntExist() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        mockMvc.perform(
                MockMvcRequestBuilders.get("/books/" +testBookEntityA.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ). andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );

    }

    @Test
    public void testThatPartialUpdateBookReturnsHttpStatus200Ok() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        bookService.createUpdateBook(testBookEntityA.getIsbn(), testBookEntityA);

        BookDto testBookA = TestDataUtil.createTestBookDtoA(null);
        testBookA.setTitle("UPDATED");
        String bookJson = objectMapper.writeValueAsString(testBookA);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/books/" + testBookEntityA.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(bookJson)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatPartialUpdateBookReturnsUpdatedBook() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        bookService.createUpdateBook(testBookEntityA.getIsbn(), testBookEntityA);

        BookDto testBookA = TestDataUtil.createTestBookDtoA(null);
        testBookA.setTitle("UPDATED");
        String bookJson = objectMapper.writeValueAsString(testBookA);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/books/" + testBookEntityA.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(bookJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.isbn").value(testBookEntityA.getIsbn())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.title").value("UPDATED")
        );
    }

    @Test
    public void testThatDeleteNonExistingBookReturnsHttpStatus204NoContent() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/books/kjsbdfjdfsk")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteExistingBookReturnsHttpStatus204NoContent() throws Exception {
        BookEntity testBookEntityA = TestDataUtil.createTestBookA(null);
        bookService.createUpdateBook(testBookEntityA.getIsbn(), testBookEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/books/" + testBookEntityA.getIsbn())
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
