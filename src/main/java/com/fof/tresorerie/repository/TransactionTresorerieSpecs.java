package com.fof.tresorerie.repository;

import com.fof.tresorerie.entity.StatutTransactionTresorerie;
import com.fof.tresorerie.entity.TransactionTresorerie;
import com.fof.tresorerie.entity.TypeTransaction;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class TransactionTresorerieSpecs {

  private TransactionTresorerieSpecs() {}

  /**
   * Liste journal : transactions postées, filtres optionnels, comptes chargés (évite N+1).
   */
  public static Specification<TransactionTresorerie> pourListeJournal(
      TypeTransaction type, Instant debutInclus, Instant finExclus, String recherche) {
    return (root, query, cb) -> {
      if (TransactionTresorerie.class.equals(query.getResultType())) {
        root.fetch("compteSource", jakarta.persistence.criteria.JoinType.LEFT);
        root.fetch("compteDestination", jakarta.persistence.criteria.JoinType.LEFT);
      }
      List<Predicate> preds = new ArrayList<>();
      preds.add(cb.equal(root.get("statut"), StatutTransactionTresorerie.POSTEE));
      if (type != null) {
        preds.add(cb.equal(root.get("type"), type));
      }
      if (debutInclus != null) {
        preds.add(cb.greaterThanOrEqualTo(root.get("dateOperation"), debutInclus));
      }
      if (finExclus != null) {
        preds.add(cb.lessThan(root.get("dateOperation"), finExclus));
      }
      if (recherche != null && !recherche.isBlank()) {
        String term = "%" + recherche.trim().toLowerCase(Locale.ROOT) + "%";
        preds.add(
            cb.or(
                cb.like(cb.lower(cb.coalesce(root.get("description"), cb.literal(""))), term),
                cb.like(cb.lower(root.get("compteSource").get("nom")), term),
                cb.like(cb.lower(root.get("compteDestination").get("nom")), term)));
      }
      return cb.and(preds.toArray(Predicate[]::new));
    };
  }
}
