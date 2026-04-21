package com.fof.reservation.repository;

import com.fof.reservation.entity.Billet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BilletRepository extends JpaRepository<Billet, Long> {

  boolean existsByNumeroBillet(String numeroBillet);
}
