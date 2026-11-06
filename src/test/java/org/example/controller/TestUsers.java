package org.example.controller;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

final class TestUsers {

    static final RequestPostProcessor USER = httpBasic("user", "User123!");
    static final RequestPostProcessor LIBRARIAN = httpBasic("librarian", "Librarian123!");
    static final RequestPostProcessor ADMIN = httpBasic("admin", "Adminamdin123!");
    static final RequestPostProcessor WRONG_PASSWORD = httpBasic("admin", "wrong-password");

    private TestUsers() {
    }
}
