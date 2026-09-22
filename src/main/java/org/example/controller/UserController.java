package org.example.controller;

import org.example.application.UserApplicationService;
import org.example.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  UserController
  @version  1.0.0 
  @since 22.09.2026 - 14.06
*/
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users
     * Отримати всіх користувачів
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id}
     * Знайти користувача за ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * GET /api/users/search?email=...
     * Знайти користувача за email
     */
    @GetMapping("/search")
    public ResponseEntity<User> getUserByEmail(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * POST /api/users
     * Додати нового користувача
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * PUT /api/users/{id}
     * Оновити дані користувача (ім'я та/або email)
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }

    /**
     * DELETE /api/users/{id}
     * Видалити користувача за ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
