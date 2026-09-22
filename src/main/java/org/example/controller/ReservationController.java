package org.example.controller;

import org.example.application.ReservationApplicationService;
import org.example.model.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  ReservationController
  @version  1.0.0 
  @since 22.09.2026 - 14.07
*/
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationApplicationService reservationService;

    public ReservationController(ReservationApplicationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * GET /api/reservations
     * Отримати всі бронювання
     */
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    /**
     * GET /api/reservations/{id}
     * Знайти бронювання за ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    /**
     * GET /api/reservations/user/{userId}
     * Отримати список бронювань конкретного користувача
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getReservationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    /**
     * POST /api/reservations?bookId=1&userId=2
     * Створити нове бронювання
     */
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestParam(name = "bookId") Long bookId,
            @RequestParam(name = "userId") Long userId) {
        Reservation newReservation = reservationService.createReservation(bookId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newReservation);
    }

    /**
     * DELETE /api/reservations/{id}
     * Скасувати бронювання (повернути книгу)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
