package com.fof.paie.repository;

import com.fof.paie.entity.Paie;
import com.fof.paie.entity.StatutPaie;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaieRepository extends JpaRepository<Paie, Long> {

  boolean existsByEmployeIdAndMois(Long employeId, String mois);

  boolean existsByEmployeId(Long employeId);

  @Query("""
      select p
      from Paie p
      join p.employe e
      where (:employeId is null or e.id = :employeId)
        and (:statut is null or p.statut = :statut)
        and (:mois is null or p.mois = :mois)
      """)
  Page<Paie> rechercher(
      @Param("employeId") Long employeId,
      @Param("statut") StatutPaie statut,
      @Param("mois") String mois,
      Pageable pageable
  );

  List<Paie> findByStatut(StatutPaie statut);

  @Query("""
      select coalesce(sum(p.salaireNet), 0)
      from Paie p
      where p.statut = com.fof.paie.entity.StatutPaie.APPROUVEE
      """)
  BigDecimal sommeSalairesApprouvesAPayer();
}

