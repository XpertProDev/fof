package com.fof.tresorerie.repository;

import com.fof.tresorerie.entity.TransactionTresorerie;
import com.fof.tresorerie.entity.TypeTransaction;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionTresorerieRepository extends JpaRepository<TransactionTresorerie, Long> {
  Page<TransactionTresorerie> findByType(TypeTransaction type, Pageable pageable);

  Page<TransactionTresorerie> findByDateOperationBetween(Instant debut, Instant fin, Pageable pageable);

  Page<TransactionTresorerie> findAllByOrderByDateOperationDesc(Pageable pageable);

  @Query(
      value = """
        select date_format(t.date_operation, '%Y-%m') as mois,
               coalesce(sum(case when t.type = 'ENTREE' then t.montant else 0 end), 0) as encaissements,
               coalesce(sum(case when t.type = 'SORTIE' then t.montant else 0 end), 0) as decaissements
        from transaction_tresorerie t
        where date(t.date_operation) between :debut and :fin
        group by date_format(t.date_operation, '%Y-%m')
        order by mois
        """,
      nativeQuery = true
  )
  List<Object[]> fluxParMois(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}

