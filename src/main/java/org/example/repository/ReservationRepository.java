package org.example.repository;

import org.example.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
  @author   VladHoryn
  @project   Code
  @class  ReservationRepository
  @version  1.0.0 
  @since 22.09.2026 - 13.57
*/
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByBookId(Long bookId);

    boolean existsByBookId(Long bookId);
}
