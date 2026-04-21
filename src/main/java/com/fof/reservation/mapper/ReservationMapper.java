package com.fof.reservation.mapper;

import com.fof.reservation.dto.AeroportInfoDto;
import com.fof.reservation.dto.BilletDto;
import com.fof.reservation.dto.CreerReservationRequest;
import com.fof.reservation.dto.EscaleDto;
import com.fof.reservation.dto.PassagerDto;
import com.fof.reservation.dto.ReservationResponse;
import com.fof.reservation.dto.VolDto;
import com.fof.reservation.entity.AeroportInfo;
import com.fof.reservation.entity.Billet;
import com.fof.reservation.entity.Escale;
import com.fof.reservation.entity.Passager;
import com.fof.reservation.entity.Reservation;
import com.fof.reservation.entity.Vol;
import java.util.ArrayList;
import java.util.List;

public final class ReservationMapper {

  private ReservationMapper() {}

  public static Reservation versEntite(CreerReservationRequest req) {
    Reservation r = new Reservation();
    r.setReferenceReservation(req.referenceReservation().trim());
    r.setDateReservation(req.dateReservation());
    r.setAgence(req.agence().trim());

    Passager p = new Passager();
    p.setPrenom(req.passager().prenom().trim());
    p.setNom(req.passager().nom().trim());
    r.setPassager(p);

    Billet b = new Billet();
    b.setNumeroBillet(req.billet().numeroBillet().trim());
    b.setType(req.billet().type().trim());
    b.setCompagnieEmission(req.billet().compagnieEmission().trim());
    r.setBillet(b);

    List<Vol> vols = new ArrayList<>();
    for (VolDto vd : req.vols()) {
      Vol v = new Vol();
      v.setReservation(r);
      v.setNumeroVol(vd.numeroVol().trim());
      v.setCompagnie(vd.compagnie().trim());
      v.setClasseVoyage(vd.classeVoyage().trim());
      v.setAvion(vd.avion() == null ? null : vd.avion().trim());
      v.setFranchiseBagage(vd.franchiseBagage() == null ? null : vd.franchiseBagage().trim());
      v.setDepart(versAeroport(vd.depart()));
      v.setArrivee(versAeroport(vd.arrivee()));

      List<EscaleDto> escales = vd.escales() == null ? List.of() : vd.escales();
      for (EscaleDto ed : escales) {
        Escale e = new Escale();
        e.setVol(v);
        e.setVilleDepart(ed.villeDepart().trim());
        e.setVilleArrivee(ed.villeArrivee().trim());
        v.getEscales().add(e);
      }
      vols.add(v);
    }
    r.setVols(vols);
    return r;
  }

  private static AeroportInfo versAeroport(AeroportInfoDto d) {
    AeroportInfo a = new AeroportInfo();
    a.setVille(d.ville().trim());
    a.setAeroport(d.aeroport().trim());
    a.setTerminal(d.terminal() == null || d.terminal().isBlank() ? null : d.terminal().trim());
    a.setDateHeure(d.dateHeure());
    return a;
  }

  public static ReservationResponse versReponse(Reservation r) {
    PassagerDto passagerDto =
        new PassagerDto(r.getPassager().getPrenom(), r.getPassager().getNom());
    BilletDto billetDto =
        new BilletDto(
            r.getBillet().getNumeroBillet(),
            r.getBillet().getType(),
            r.getBillet().getCompagnieEmission());

    List<ReservationResponse.VolResponse> volResponses = new ArrayList<>();
    for (Vol v : r.getVols()) {
      List<ReservationResponse.EscaleResponse> esc = new ArrayList<>();
      for (Escale e : v.getEscales()) {
        esc.add(new ReservationResponse.EscaleResponse(e.getId(), e.getVilleDepart(), e.getVilleArrivee()));
      }
      volResponses.add(
          new ReservationResponse.VolResponse(
              v.getId(),
              v.getNumeroVol(),
              v.getCompagnie(),
              v.getClasseVoyage(),
              v.getAvion(),
              v.getFranchiseBagage(),
              versAeroportReponse(v.getDepart()),
              versAeroportReponse(v.getArrivee()),
              esc));
    }

    return new ReservationResponse(
        r.getId(),
        r.getReferenceReservation(),
        r.getDateReservation(),
        r.getAgence(),
        passagerDto,
        billetDto,
        volResponses);
  }

  private static ReservationResponse.AeroportInfoResponse versAeroportReponse(AeroportInfo a) {
    if (a == null) {
      return null;
    }
    return new ReservationResponse.AeroportInfoResponse(
        a.getVille(), a.getAeroport(), a.getTerminal(), a.getDateHeure());
  }
}
