package com.fof.reservation.repository;

import com.fof.reservation.entity.Reservation;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecs {

  private ReservationSpecs() {}

  public static Specification<Reservation> recherche(String recherche) {
    return (root, query, cb) -> {
      if (recherche == null || recherche.isBlank()) {
        return cb.conjunction();
      }

      // Important: on joint vols (collection) pour filtrer, donc distinct pour éviter les doublons.
      query.distinct(true);

      var passager = root.join("passager", jakarta.persistence.criteria.JoinType.LEFT);
      var billet = root.join("billet", jakarta.persistence.criteria.JoinType.LEFT);
      var vols = root.join("vols", jakarta.persistence.criteria.JoinType.LEFT);

      String term = "%" + recherche.trim().toLowerCase(Locale.ROOT) + "%";

      return cb.or(
          cb.like(cb.lower(root.get("referenceReservation")), term),
          cb.like(cb.lower(root.get("agence")), term),
          cb.like(cb.lower(passager.get("prenom")), term),
          cb.like(cb.lower(passager.get("nom")), term),
          cb.like(cb.lower(billet.get("numeroBillet")), term),
          cb.like(cb.lower(vols.get("numeroVol")), term),
          cb.like(cb.lower(vols.get("compagnie")), term)
      );
    };
  }
}

