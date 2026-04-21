package com.fof.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
    Long id,
    String referenceReservation,
    LocalDate dateReservation,
    String agence,
    PassagerDto passager,
    BilletDto billet,
    List<VolResponse> vols
) {

  public record VolResponse(
      Long id,
      String numeroVol,
      String compagnie,
      String classeVoyage,
      String avion,
      String franchiseBagage,
      AeroportInfoResponse depart,
      AeroportInfoResponse arrivee,
      List<EscaleResponse> escales
  ) {}

  public record AeroportInfoResponse(
      String ville,
      String aeroport,
      String terminal,
      LocalDateTime dateHeure
  ) {}

  public record EscaleResponse(Long id, String villeDepart, String villeArrivee) {}
}
