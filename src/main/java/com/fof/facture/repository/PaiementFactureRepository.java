package com.fof.facture.repository;

import com.fof.facture.entity.PaiementFacture;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaiementFactureRepository extends JpaRepository<PaiementFacture, Long> {

  /** CA encaissé par mois calendaire (date du paiement), factures non annulées. */
  @Query(
      value = """
        select date_format(p.date_paiement, '%Y-%m') as mois,
               coalesce(sum(p.montant), 0) as total
        from paiement_facture p
        inner join facture f on f.id = p.facture_id
        where date(p.date_paiement) between :debut and :fin
          and f.statut <> 'ANNULEE'
        group by date_format(p.date_paiement, '%Y-%m')
        order by mois
        """,
      nativeQuery = true
  )
  List<Object[]> chiffreAffairesEncaisseParMois(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

  @Query("""
      select coalesce(sum(p.montant), 0)
      from PaiementFacture p
      join p.facture f
      where f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and p.datePaiement >= :debutInstant
        and p.datePaiement < :finExclusInstant
      """)
  BigDecimal sommeMontantPaiementsEntre(
      @Param("debutInstant") Instant debutInstant,
      @Param("finExclusInstant") Instant finExclusInstant
  );
}

