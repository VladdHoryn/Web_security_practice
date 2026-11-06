package org.example.controller;

import org.example.application.BookApplicationService;
import org.example.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  BookController
  @version  1.0.0 
  @since 22.09.2026 - 14.01
*/
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookApplicationService bookService;

    public BookController(BookApplicationService bookService) {
        this.bookService = bookService;
    }

    /**
     * GET /api/books
     * Отримати всі книги
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    /**
     * GET /api/books/available
     * Отримати лише доступні (незаброньовані) книги
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }

    /**
     * GET /api/books/{id}
     * Знайти книгу за ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    /**
     * POST /api/books
     * Додати нову книгу
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book createdBook = bookService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * PATCH /api/books/{id}/status?reserved=true
     * Змінити статус бронювання книги
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Book> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam(name = "reserved") boolean status) {
        Book updatedBook = bookService.updateReservationStatus(id, status);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * DELETE /api/books/{id}
     * Видалити книгу за ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
