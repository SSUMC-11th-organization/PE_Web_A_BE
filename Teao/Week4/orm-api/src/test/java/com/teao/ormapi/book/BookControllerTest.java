package com.teao.ormapi.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.teao.ormapi.common.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void getBooksReturnsLatestBooks() throws Exception {
        given(bookService.getBooks()).willReturn(List.of(
                new BookResponse(2L, "스프링", "설명", "컴퓨터", true)
        ));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("컴퓨터"))
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }

    @Test
    void createBookReturnsCreated() throws Exception {
        given(bookService.createBook(any())).willReturn(
                new BookResponse(4L, "JPA 입문", "설명", "컴퓨터", true)
        );

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":2,"title":"JPA 입문","description":"설명"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(4));
    }

    @Test
    void emptyTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":2,"title":"","description":"설명"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
    }

    @Test
    void unknownCategoryReturnsNotFound() throws Exception {
        given(bookService.createBook(any()))
                .willThrow(new ResourceNotFoundException("존재하지 않는 카테고리입니다."));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":999,"title":"JPA 입문","description":"설명"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다."));
    }
}
