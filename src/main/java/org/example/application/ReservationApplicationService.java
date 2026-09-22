package org.example.application;

import org.example.model.Book;
import org.example.model.Reservation;
import org.example.model.User;
import org.example.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  ReservationApplicationService
  @version  1.0.0 
  @since 22.09.2026 - 13.58
*/
@Service
public class ReservationApplicationService {

    private final ReservationRepository reservationRepository;
    private final BookApplicationService bookService;
    private final UserApplicationService userService;

    public ReservationApplicationService(
            ReservationRepository reservationRepository,
            BookApplicationService bookService,
            UserApplicationService userService) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Отримати всі бронювання
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Знайти бронювання за ID
     */
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Бронювання з ID " + id + " не знайдено"));
    }

    /**
     * Отримати список бронювань конкретного користувача
     */
    public List<Reservation> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * Створити нове бронювання
     */
    @Transactional
    public Reservation createReservation(Long bookId, Long userId) {
        Book book = bookService.getBookById(bookId);

        if (book.isReserved()) {
            throw new IllegalStateException("Книга з ID " + bookId + " вже заброньована.");
        }

        User user = userService.getUserById(userId);

        bookService.updateReservationStatus(bookId, true);

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservationDate(LocalDate.now());

        return reservationRepository.save(reservation);
    }

    /**
     * Скасувати бронювання (повернути книгу)
     */
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);

        Book book = reservation.getBook();
        bookService.updateReservationStatus(book.getId(), false);

        reservationRepository.deleteById(reservationId);
    }
}