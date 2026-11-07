package org.example.controller;

import org.example.application.AuthService;
import org.example.application.AuthService.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Тіло: { "username": "...", "password": "..." } → { "token": "...", ... }
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody Credentials credentials) {
        return ResponseEntity.ok(authService.login(credentials.username(), credentials.password()));
    }

    /**
     * POST /api/auth/register
     * Створює новий обліковий запис з роллю USER і одразу повертає токен.
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody Credentials credentials) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(credentials.username(), credentials.password()));
    }

    public record Credentials(String username, String password) {
    }
}
