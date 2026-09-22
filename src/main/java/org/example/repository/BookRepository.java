package org.example.repository;

import org.example.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  BookRepository
  @version  1.0.0 
  @since 22.09.2026 - 13.49
*/
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsReserved(boolean isReserved);

}
