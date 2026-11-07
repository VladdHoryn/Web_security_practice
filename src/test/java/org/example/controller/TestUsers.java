package org.example.controller;

import org.example.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Додає до запиту заголовок "Authorization: Bearer <jwt>".
 * Токен генерується справжнім JwtService з тестового контексту,
 * тому перевіряється весь ланцюжок: фільтр → підпис → роль → @PreAuthorize.
 */
final class TestUsers {

    static final RequestPostProcessor USER = bearer("user", "USER");
    static final RequestPostProcessor LIBRARIAN = bearer("librarian", "LIBRARIAN");
    static final RequestPostProcessor ADMIN = bearer("admin", "ADMIN");
    static final RequestPostProcessor INVALID_TOKEN = request -> {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid.jwt.token");
        return request;
    };

    private TestUsers() {
    }

    private static RequestPostProcessor bearer(String username, String role) {
        return request -> {
            JwtService jwtService = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(request.getServletContext())
                    .getBean(JwtService.class);
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(username, role));
            return request;
        };
    }
}
