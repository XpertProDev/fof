package com.fof.reservation.service;

import com.fof.reservation.dto.CreerReservationRequest;
import com.fof.reservation.dto.ReservationResponse;
import com.fof.reservation.entity.Reservation;
import com.fof.reservation.mapper.ReservationMapper;
import com.fof.reservation.repository.BilletRepository;
import com.fof.reservation.repository.ReservationRepository;
import com.fof.reservation.repository.ReservationSpecs;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final BilletRepository billetRepository;

  @Transactional
  public ReservationResponse creer(CreerReservationRequest request) {
    String ref = request.referenceReservation().trim();
    String numBillet = request.billet().numeroBillet().trim();
    if (reservationRepository.existsByReferenceReservation(ref)) {
      throw new ValidationException("Une réservation existe déjà avec cette référence");
    }
    if (billetRepository.existsByNumeroBillet(numBillet)) {
      throw new ValidationException("Un billet avec ce numéro existe déjà");
    }
    Reservation entity = ReservationMapper.versEntite(request);
    Reservation sauve = reservationRepository.save(entity);
    return ReservationMapper.versReponse(sauve);
  }

  @Transactional(readOnly = true)
  public ReservationResponse obtenir(Long id) {
    Reservation r = reservationRepository.findById(id).orElseThrow(() -> entiteInconnue(id));
    return ReservationMapper.versReponse(r);
  }

  @Transactional(readOnly = true)
  public Page<ReservationResponse> lister(String recherche, Pageable pageable) {
    Specification<Reservation> spec = ReservationSpecs.recherche(recherche);
    return reservationRepository.findAll(spec, pageable).map(ReservationMapper::versReponse);
  }

  @Transactional
  public void supprimer(Long id) {
    if (!reservationRepository.existsById(id)) {
      throw entiteInconnue(id);
    }
    reservationRepository.deleteById(id);
  }

  private static EntityNotFoundException entiteInconnue(Long id) {
    return new EntityNotFoundException("Réservation introuvable: " + id);
  }
}
