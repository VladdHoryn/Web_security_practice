package org.example.application;

import org.example.model.Book;
import org.example.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/*
  @author   VladHoryn
  @project   Code
  @class  BookApplicationService
  @version  1.0.0 
  @since 22.09.2026 - 13.51
*/
@Service
public class BookApplicationService {

    private final BookRepository bookRepository;
    public BookApplicationService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Отримати всі книги
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Отримати лише доступні (незаброньовані) книги
     */
    public List<Book> getAvailableBooks() {
        return bookRepository.findByIsReserved(false);
    }

    /**
     * Знайти книгу за ID
     */
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Книгу з ID " + id + " не знайдено"));
    }

    /**
     * Додати нову книгу
     */
    @Transactional
    public Book addBook(Book book) {
        book.setReserved(false);
        return bookRepository.save(book);
    }

    /**
     * Змінити статус бронювання книги
     */
    @Transactional
    public Book updateReservationStatus(Long id, boolean status) {
        Book book = getBookById(id);

        if (book.isReserved() == status) {
            return book;
        }

        book.setReserved(status);

        return bookRepository.save(book);
    }

    /**
     * Видалити книгу за ID
     */
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Не неможливо видалити: книгу з ID " + id + " не знайдено");
        }
        bookRepository.deleteById(id);
    }
}
