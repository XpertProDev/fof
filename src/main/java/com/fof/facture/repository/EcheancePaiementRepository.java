package com.fof.facture.repository;

import com.fof.facture.entity.EcheancePaiement;
import com.fof.facture.entity.StatutEcheancePaiement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EcheancePaiementRepository extends JpaRepository<EcheancePaiement, Long> {
  Page<EcheancePaiement> findByFactureId(Long factureId, Pageable pageable);

  List<EcheancePaiement> findByFactureIdAndStatutOrderByDatePrevueAsc(Long factureId, StatutEcheancePaiement statut);

  List<EcheancePaiement> findByStatutAndDatePrevueBetween(StatutEcheancePaiement statut, LocalDate debut, LocalDate fin);

  List<EcheancePaiement> findByStatutAndDatePrevueBefore(StatutEcheancePaiement statut, LocalDate date);

  @Query("""
      select count(e)
      from EcheancePaiement e
      join e.facture f
      where e.statut = com.fof.facture.entity.StatutEcheancePaiement.EN_ATTENTE
        and e.datePrevue between :debut and :fin
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
      """)
  long compterEcheancesPlanEnAttenteEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

  @Query("""
      select coalesce(sum(e.montantProgramme - e.montantPaye), 0)
      from EcheancePaiement e
      join e.facture f
      where e.statut = com.fof.facture.entity.StatutEcheancePaiement.EN_ATTENTE
        and e.datePrevue between :debut and :fin
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
      """)
  BigDecimal sommeRestantProgrammeEcheancesEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}

