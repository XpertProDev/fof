package com.fof.reservation.controller;

import com.fof.reservation.dto.CreerReservationRequest;
import com.fof.reservation.dto.ReservationResponse;
import com.fof.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservationService reservationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('PERM_RESERVATION_MANAGE')")
  public ReservationResponse creer(@Valid @RequestBody CreerReservationRequest request) {
    return reservationService.creer(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_RESERVATION_MANAGE')")
  public ReservationResponse obtenir(@PathVariable Long id) {
    return reservationService.obtenir(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_RESERVATION_MANAGE')")
  public Page<ReservationResponse> lister(
      @RequestParam(required = false) String recherche,
      @PageableDefault(size = 20, sort = {"dateReservation", "id"}, direction = Sort.Direction.DESC)
          Pageable pageable) {
    return reservationService.lister(recherche, pageable);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('PERM_RESERVATION_MANAGE')")
  public void supprimer(@PathVariable Long id) {
    reservationService.supprimer(id);
  }
}
