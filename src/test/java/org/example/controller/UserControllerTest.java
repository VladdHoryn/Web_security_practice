package org.example.controller;

import org.example.application.UserApplicationService;
import org.example.config.SecurityConfig;
import org.example.model.User;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    private static final String NEW_USER_JSON = """
            {
              "name": "John Doe",
              "email": "test@example.com"
            }
            """;

    private static final String UPDATED_USER_JSON = """
            {
              "name": "John Doe Updated",
              "email": "updated.test@example.com"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserApplicationService userService;

    // ---------- GET /api/users ----------

    @Test
    @DisplayName("GET /api/users: ADMIN отримує всіх користувачів")
    void getAll_asAdmin_returnsUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new User(1L, "John Doe", "test@example.com"),
                new User(2L, "Jane Doe", "jane@example.com")));

        mockMvc.perform(get("/api/users").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }

    @Test
    @DisplayName("GET /api/users: LIBRARIAN → 403")
    void getAll_asLibrarian_forbidden() throws Exception {
        mockMvc.perform(get("/api/users").with(LIBRARIAN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/users: USER → 403")
    void getAll_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/users").with(USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/users: без авторизації → 401")
    void getAll_withoutAuth_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    // ---------- GET /api/users/{id} ----------

    @Test
    @DisplayName("GET /api/users/{id}: ADMIN отримує користувача за ID")
    void getById_asAdmin_returnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User(1L, "John Doe", "test@example.com"));

        mockMvc.perform(get("/api/users/1").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/users/{id}: LIBRARIAN → 403")
    void getById_asLibrarian_forbidden() throws Exception {
        mockMvc.perform(get("/api/users/1").with(LIBRARIAN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ---------- GET /api/users/search ----------

    @Test
    @DisplayName("GET /api/users/search: ADMIN знаходить користувача за email")
    void search_asAdmin_returnsUser() throws Exception {
        when(userService.getUserByEmail("test@example.com"))
                .thenReturn(new User(1L, "John Doe", "test@example.com"));

        mockMvc.perform(get("/api/users/search").param("email", "test@example.com").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/search: USER → 403")
    void search_asUser_forbidden() throws Exception {
        mockMvc.perform(get("/api/users/search").param("email", "test@example.com").with(USER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ---------- POST /api/users ----------

    @Test
    @DisplayName("POST /api/users: ADMIN створює користувача → 201")
    void create_asAdmin_created() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenReturn(new User(3L, "John Doe", "test@example.com"));

        mockMvc.perform(post("/api/users").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/users: LIBRARIAN → 403")
    void create_asLibrarian_forbidden() throws Exception {
        mockMvc.perform(post("/api/users").with(LIBRARIAN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_USER_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ---------- PUT /api/users/{id} ----------

    @Test
    @DisplayName("PUT /api/users/{id}: ADMIN оновлює користувача")
    void update_asAdmin_ok() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(new User(1L, "John Doe Updated", "updated.test@example.com"));

        mockMvc.perform(put("/api/users/1").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATED_USER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe Updated"))
                .andExpect(jsonPath("$.email").value("updated.test@example.com"));

        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id}: USER → 403")
    void update_asUser_forbidden() throws Exception {
        mockMvc.perform(put("/api/users/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATED_USER_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // ---------- DELETE /api/users/{id} ----------

    @Test
    @DisplayName("DELETE /api/users/{id}: ADMIN видаляє користувача → 204")
    void delete_asAdmin_noContent() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(ADMIN))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /api/users/{id}: LIBRARIAN → 403")
    void delete_asLibrarian_forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(LIBRARIAN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
