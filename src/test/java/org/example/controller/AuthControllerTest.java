package org.example.controller;

import org.example.application.AuthService;
import org.example.config.SecurityConfig;
import org.example.model.Account;
import org.example.repository.AccountRepository;
import org.example.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthService і JwtService — справжні, підмінюється лише репозиторій (БД).
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtService.class, AuthService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AccountRepository accountRepository;

    private static String credentials(String username, String password) {
        return """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password);
    }

    @Test
    @DisplayName("POST /api/auth/login: правильні дані → 200 і валідний JWT з роллю")
    void login_validCredentials_returnsToken() throws Exception {
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(
                new Account(1L, "admin", passwordEncoder.encode("Adminamdin123!"), Account.Role.ADMIN)));

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin", "Adminamdin123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();

        String token = body.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        assertThat(jwtService.parse(token))
                .hasValue(new JwtService.TokenData("admin", "ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login: неправильний пароль → 401")
    void login_wrongPassword_unauthorized() throws Exception {
        when(accountRepository.findByUsername("admin")).thenReturn(Optional.of(
                new Account(1L, "admin", passwordEncoder.encode("Adminamdin123!"), Account.Role.ADMIN)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login: неіснуючий логін → 401")
    void login_unknownUser_unauthorized() throws Exception {
        when(accountRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("ghost", "whatever123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register: новий логін → 201, роль USER, пароль збережено хешем")
    void register_newUser_created() throws Exception {
        when(accountRepository.existsByUsername("newbie")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("newbie", "Newbie123!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        verify(accountRepository).save(argThat(a ->
                a.getRole() == Account.Role.USER
                        && !a.getPassword().equals("Newbie123!")
                        && passwordEncoder.matches("Newbie123!", a.getPassword())));
    }

    @Test
    @DisplayName("POST /api/auth/register: зайнятий логін → 409")
    void register_existingUser_conflict() throws Exception {
        when(accountRepository.existsByUsername("admin")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin", "Something123!")))
                .andExpect(status().isConflict());

        verify(accountRepository, never()).save(any());
    }
}
