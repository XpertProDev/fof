package com.fof.tresorerie.repository;

import com.fof.tresorerie.entity.Compte;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompteRepository extends JpaRepository<Compte, Long> {
  Page<Compte> findByNomContainingIgnoreCase(String nom, Pageable pageable);

  Optional<Compte> findByNomIgnoreCase(String nom);

  List<Compte> findBySoldeActuelLessThan(BigDecimal seuil);

  @Query("select coalesce(sum(c.soldeActuel), 0) from Compte c")
  BigDecimal sommeSoldeGlobal();
}

