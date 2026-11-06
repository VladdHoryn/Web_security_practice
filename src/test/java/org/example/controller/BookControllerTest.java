package org.example.controller;

import org.example.application.BookApplicationService;
import org.example.config.SecurityConfig;
import org.example.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.example.controller.TestUsers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    private static final String NEW_BOOK_JSON = """
            {
              "title": "Clean Code",
              "author": "Robert C. Martin"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookApplicationService bookService;

    private static Book book(long id, String title, boolean reserved) {
        return new Book(id, title, "Robert C. Martin", reserved);
    }

    // ---------- GET /api/books ----------

    @Test
    @DisplayName("GET /api/books: USER отримує список усіх книг")
    void getAllBooks_asUser_returnsBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(
                book(1, "Clean Code", false),
                book(2, "Clean Architecture", true)));

        mockMvc.perform(get("/api/books").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[1].reserved").value(true));
    }

    @Test
    @DisplayName("GET /api/books: LIBRARIAN і ADMIN теж мають доступ")
    void getAllBooks_asLibrarianAndAdmin_ok() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/books").with(LIBRARIAN)).andExpect(status().isOk());
        mockMvc.perform(get("/api/books").with(ADMIN)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/books: без авторизації → 401")
    void getAllBooks_withoutAuth_unauthorized() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("GET /api/books: неправильний пароль → 401")
    void getAllBooks_wrongPassword_unauthorized() throws Exception {
        mockMvc.perform(get("/api/books").with(WRONG_PASSWORD))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    // ---------- GET /api/books/available ----------

    @Test
    @DisplayName("GET /api/books/available: USER отримує лише доступні книги")
    void getAvailableBooks_asUser_returnsAvailable() throws Exception {
        when(bookService.getAvailableBooks()).thenReturn(List.of(book(1, "Clean Code", false)));

        mockMvc.perform(get("/api/books/available").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reserved").value(false));
    }

    // ---------- GET /api/books/{id} ----------

    @Test
    @DisplayName("GET /api/books/{id}: USER отримує книгу за ID")
    void getBookById_asUser_returnsBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(book(1, "Clean Code", false));

        mockMvc.perform(get("/api/books/1").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    // ---------- POST /api/books ----------

    @Test
    @DisplayName("POST /api/books: LIBRARIAN створює книгу → 201")
    void addBook_asLibrarian_created() throws Exception {
        when(bookService.addBook(any(Book.class))).thenReturn(book(10, "Clean Code", false));

        mockMvc.perform(post("/api/books").with(LIBRARIAN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_BOOK_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        verify(bookService).addBook(any(Book.class));
    }

    @Test
    @DisplayName("POST /api/books: ADMIN створює книгу → 201")
    void addBook_asAdmin_created() throws Exception {
        when(bookService.addBook(any(Book.class))).thenReturn(book(10, "Clean Code", false));

        mockMvc.perform(post("/api/books").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_BOOK_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/books: USER → 403")
    void addBook_asUser_forbidden() throws Exception {
        mockMvc.perform(post("/api/books").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_BOOK_JSON))
                .andExpect(status().isForbidden());

        verify(bookService, never()).addBook(any());
    }

    // ---------- PATCH /api/books/{id}/status ----------

    @Test
    @DisplayName("PATCH /api/books/{id}/status: LIBRARIAN змінює статус")
    void updateStatus_asLibrarian_ok() throws Exception {
        when(bookService.updateReservationStatus(1L, true)).thenReturn(book(1, "Clean Code", true));

        mockMvc.perform(patch("/api/books/1/status").param("reserved", "true").with(LIBRARIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserved").value(true));

        verify(bookService).updateReservationStatus(1L, true);
    }

    @Test
    @DisplayName("PATCH /api/books/{id}/status: USER → 403")
    void updateStatus_asUser_forbidden() throws Exception {
        mockMvc.perform(patch("/api/books/1/status").param("reserved", "true").with(USER))
                .andExpect(status().isForbidden());

        verify(bookService, never()).updateReservationStatus(anyLong(), anyBoolean());
    }

    // ---------- DELETE /api/books/{id} ----------

    @Test
    @DisplayName("DELETE /api/books/{id}: ADMIN видаляє книгу → 204")
    void deleteBook_asAdmin_noContent() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(ADMIN))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("DELETE /api/books/{id}: LIBRARIAN → 403")
    void deleteBook_asLibrarian_forbidden() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(LIBRARIAN))
                .andExpect(status().isForbidden());

        verify(bookService, never()).deleteBook(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/books/{id}: USER → 403")
    void deleteBook_asUser_forbidden() throws Exception {
        mockMvc.perform(delete("/api/books/1").with(USER))
                .andExpect(status().isForbidden());

        verify(bookService, never()).deleteBook(anyLong());
    }
}
