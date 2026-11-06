package org.example.controller;

import org.example.application.ReservationApplicationService;
import org.example.config.SecurityConfig;
import org.example.model.Book;
import org.example.model.Reservation;
import org.example.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.example.controller.TestUsers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationApplicationService reservationService;

    private static Reservation reservation(long id, long bookId, long userId) {
        Book book = new Book(bookId, "Clean Code", "Robert C. Martin", true);
        User user = new User(userId, "John Doe", "test@example.com");
        return new Reservation(id, book, user, LocalDate.of(2026, 11, 6));
    }

    // ---------- GET /api/reservations ----------

    @Test
    @DisplayName("GET /api/reservations: LIBRARIAN отримує всі бронювання")
    void getAll_asLibrarian_returnsReservations() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(reservation(1, 1, 1)));

        mockMvc.perform(get("/api/reservations").with(LIBRARIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].book.id").value(1))
                .andExpect(jsonPath("$[0].user.id").value(1))
                .andExpect(jsonPath("$[0].reservationDate").value("2026-11-06"));
    }

    @Test
    @DisplayName("GET /api/reservations: ADMIN має доступ")
    void getAll_asAdmin_ok() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of());

        mockMvc.perform(get("/api/reservations").with(ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reservations: USER → 403")
    void getAll_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/reservations").with(USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("GET /api/reservations: без авторизації → 401")
    void getAll_withoutAuth_unauthorized() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservationService);
    }

    // ---------- GET /api/reservations/{id} ----------

    @Test
    @DisplayName("GET /api/reservations/{id}: LIBRARIAN отримує бронювання за ID")
    void getById_asLibrarian_returnsReservation() throws Exception {
        when(reservationService.getReservationById(5L)).thenReturn(reservation(5, 1, 1));

        mockMvc.perform(get("/api/reservations/5").with(LIBRARIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET /api/reservations/{id}: USER → 403")
    void getById_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/reservations/5").with(USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }

    // ---------- GET /api/reservations/user/{userId} ----------

    @Test
    @DisplayName("GET /api/reservations/user/{userId}: LIBRARIAN отримує бронювання користувача")
    void getByUser_asLibrarian_returnsReservations() throws Exception {
        when(reservationService.getReservationsByUser(1L))
                .thenReturn(List.of(reservation(1, 1, 1), reservation(2, 2, 1)));

        mockMvc.perform(get("/api/reservations/user/1").with(LIBRARIAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].book.id").value(2));
    }

    @Test
    @DisplayName("GET /api/reservations/user/{userId}: USER → 403")
    void getByUser_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/reservations/user/1").with(USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservationService);
    }

    // ---------- POST /api/reservations ----------

    @Test
    @DisplayName("POST /api/reservations: USER створює бронювання → 201")
    void create_asUser_created() throws Exception {
        when(reservationService.createReservation(1L, 1L)).thenReturn(reservation(7, 1, 1));

        mockMvc.perform(post("/api/reservations")
                        .param("bookId", "1")
                        .param("userId", "1")
                        .with(USER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.book.reserved").value(true));

        verify(reservationService).createReservation(1L, 1L);
    }

    @Test
    @DisplayName("POST /api/reservations: LIBRARIAN і ADMIN теж можуть створювати")
    void create_asLibrarianAndAdmin_created() throws Exception {
        when(reservationService.createReservation(1L, 1L)).thenReturn(reservation(7, 1, 1));

        mockMvc.perform(post("/api/reservations").param("bookId", "1").param("userId", "1").with(LIBRARIAN))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/reservations").param("bookId", "1").param("userId", "1").with(ADMIN))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/reservations: без авторизації → 401")
    void create_withoutAuth_unauthorized() throws Exception {
        mockMvc.perform(post("/api/reservations").param("bookId", "1").param("userId", "1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservationService);
    }

    // ---------- DELETE /api/reservations/{id} ----------

    @Test
    @DisplayName("DELETE /api/reservations/{id}: LIBRARIAN скасовує бронювання → 204")
    void cancel_asLibrarian_noContent() throws Exception {
        mockMvc.perform(delete("/api/reservations/1").with(LIBRARIAN))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L);
    }

    @Test
    @DisplayName("DELETE /api/reservations/{id}: USER → 403")
    void cancel_asUser_forbidden() throws Exception {
        mockMvc.perform(delete("/api/reservations/1").with(USER))
                .andExpect(status().isForbidden());

        verify(reservationService, never()).cancelReservation(anyLong());
    }
}
