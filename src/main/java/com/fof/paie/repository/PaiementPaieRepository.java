package com.fof.paie.repository;

import com.fof.paie.entity.PaiementPaie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementPaieRepository extends JpaRepository<PaiementPaie, Long> {
  List<PaiementPaie> findByPaieId(Long paieId);
}

