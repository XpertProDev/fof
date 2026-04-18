package com.fof.facture.repository;

import com.fof.facture.entity.EcheancePaiement;
import com.fof.facture.entity.StatutEcheancePaiement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcheancePaiementRepository extends JpaRepository<EcheancePaiement, Long> {
  Page<EcheancePaiement> findByFactureId(Long factureId, Pageable pageable);

  List<EcheancePaiement> findByFactureIdAndStatutOrderByDatePrevueAsc(Long factureId, StatutEcheancePaiement statut);

  List<EcheancePaiement> findByStatutAndDatePrevueBetween(StatutEcheancePaiement statut, LocalDate debut, LocalDate fin);

  List<EcheancePaiement> findByStatutAndDatePrevueBefore(StatutEcheancePaiement statut, LocalDate date);
}

