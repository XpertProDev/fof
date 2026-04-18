package com.fof.depense.repository;

import com.fof.depense.entity.Depense;
import com.fof.depense.entity.StatutDepense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

  @Query("""
      select d
      from Depense d
      where (:statut is null or d.statut = :statut)
        and (:debut is null or d.dateDepense >= :debut)
        and (:fin is null or d.dateDepense <= :fin)
        and (
          :q is null
          or lower(d.titre) like lower(concat('%', :q, '%'))
          or lower(coalesce(d.categorie, '')) like lower(concat('%', :q, '%'))
        )
      """)
  Page<Depense> rechercher(
      @Param("q") String q,
      @Param("statut") StatutDepense statut,
      @Param("debut") LocalDate debut,
      @Param("fin") LocalDate fin,
      Pageable pageable
  );

  @Query("""
      select coalesce(sum(d.montant), 0)
      from Depense d
      where d.statut = com.fof.depense.entity.StatutDepense.PAYEE
        and d.dateDepense >= :debut
        and d.dateDepense <= :fin
      """)
  BigDecimal sommeDepensesEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

  @Query(
      value = """
        select date_format(d.date_depense, '%Y-%m') as mois,
               coalesce(sum(d.montant), 0) as total
        from depense d
        where d.statut = 'PAYEE'
          and d.date_depense between :debut and :fin
        group by date_format(d.date_depense, '%Y-%m')
        order by mois
        """,
      nativeQuery = true
  )
  List<Object[]> depensesParMois(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}

