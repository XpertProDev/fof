package com.fof.facture.repository;

import com.fof.facture.entity.Facture;
import com.fof.facture.entity.StatutFacture;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FactureRepository extends JpaRepository<Facture, Long> {

  @Query("""
      select f
      from Facture f
      join f.client c
      where (:clientId is null or c.id = :clientId)
        and (:statut is null or f.statut = :statut)
        and (
          :q is null
          or lower(f.numero) like lower(concat('%', :q, '%'))
          or lower(c.nomComplet) like lower(concat('%', :q, '%'))
        )
      """)
  Page<Facture> rechercher(
      @Param("q") String q,
      @Param("clientId") Long clientId,
      @Param("statut") StatutFacture statut,
      Pageable pageable
  );

  @Query("""
      select count(f)
      from Facture f
      join f.client c
      where c.id = :clientId
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
      """)
  long compterFacturesPourClientHorsAnnulees(@Param("clientId") Long clientId);

  @Query("""
      select count(f)
      from Facture f
      join f.client c
      where c.id = :clientId
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and f.montantRestant = 0
      """)
  long compterFacturesPayeesPourClient(@Param("clientId") Long clientId);

  @Query("""
      select count(f)
      from Facture f
      join f.client c
      where c.id = :clientId
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and f.statut <> com.fof.facture.entity.StatutFacture.BROUILLON
        and f.montantRestant > 0
      """)
  long compterFacturesImpayeesPourClient(@Param("clientId") Long clientId);

  /**
   * @param filtrePayee {@code null} = toutes (hors annulées), {@code true} = soldées, {@code false} = reste à payer
   *     (hors brouillon)
   */
  @Query("""
      select f
      from Facture f
      join f.client c
      where c.id = :clientId
        and f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and (
          :q is null
          or lower(f.numero) like lower(concat('%', :q, '%'))
        )
        and (
          :filtrePayee is null
          or (:filtrePayee = true and f.montantRestant = 0)
          or (:filtrePayee = false and f.montantRestant > 0 and f.statut <> com.fof.facture.entity.StatutFacture.BROUILLON)
        )
      """)
  Page<Facture> rechercherPourClient(
      @Param("clientId") Long clientId,
      @Param("q") String q,
      @Param("filtrePayee") Boolean filtrePayee,
      Pageable pageable
  );

  List<Facture> findByStatutInAndDateEcheanceBetween(List<StatutFacture> statuts, LocalDate debut, LocalDate fin);

  List<Facture> findByStatutInAndDateEcheanceBefore(List<StatutFacture> statuts, LocalDate date);

  @Query("""
      select coalesce(sum(f.totalTtc), 0)
      from Facture f
      where f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
      """)
  BigDecimal sommeChiffreAffairesTotal();

  @Query("""
      select coalesce(sum(f.montantRestant), 0)
      from Facture f
      where f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and f.montantRestant > 0
      """)
  BigDecimal sommeDettesClients();

  @Query("""
      select count(f)
      from Facture f
      where f.statut <> com.fof.facture.entity.StatutFacture.ANNULEE
        and f.montantRestant > 0
      """)
  long compterFacturesEnAttente();

  @Query(
      value = """
        select date_format(f.date_emission, '%Y-%m') as mois,
               coalesce(sum(f.total_ttc), 0) as total
        from facture f
        where f.date_emission between :debut and :fin
          and f.statut <> 'ANNULEE'
        group by date_format(f.date_emission, '%Y-%m')
        order by mois
        """,
      nativeQuery = true
  )
  List<Object[]> chiffreAffairesParMois(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

  @Query(
      value = """
        select f.statut as statut, count(*) as total
        from facture f
        where f.statut <> 'ANNULEE'
        group by f.statut
        """,
      nativeQuery = true
  )
  List<Object[]> compterParStatut();
}

