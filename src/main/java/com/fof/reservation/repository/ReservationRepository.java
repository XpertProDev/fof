package com.fof.reservation.repository;

import com.fof.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservationRepository
    extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

  boolean existsByReferenceReservation(String referenceReservation);

  Page<Reservation> findAllByOrderByDateReservationDescIdDesc(Pageable pageable);
}
