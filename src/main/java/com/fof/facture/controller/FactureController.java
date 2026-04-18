package com.fof.facture.controller;

import com.fof.facture.dto.CreerFactureAvecPaiementRequest;
import com.fof.facture.dto.CreerFactureRequest;
import com.fof.facture.dto.FactureResponse;
import com.fof.facture.dto.PaiementFactureRequest;
import com.fof.facture.entity.StatutFacture;
import com.fof.facture.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/factures")
public class FactureController {

  private final FactureService factureService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERM_FACTURE_CREATE')")
  public FactureResponse creer(@Valid @RequestBody CreerFactureRequest request) {
    return factureService.creer(request);
  }

  /**
   * Création facture + paiement initial optionnel en une requête.
   */
  @PostMapping("/avec-paiement")
  @PreAuthorize("hasAuthority('PERM_FACTURE_CREATE') and (hasAuthority('PERM_FACTURE_UPDATE') or hasAuthority('PERM_TREASURY_MANAGE'))")
  public FactureResponse creerAvecPaiement(@Valid @RequestBody CreerFactureAvecPaiementRequest request) {
    return factureService.creerAvecPaiement(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_FACTURE_READ')")
  public FactureResponse detail(@PathVariable Long id) {
    return factureService.detail(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_FACTURE_READ')")
  public Page<FactureResponse> lister(
      String recherche,
      Long clientId,
      StatutFacture statut,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return factureService.lister(recherche, clientId, statut, pageable);
  }

  @PostMapping("/{id}/paiements")
  @PreAuthorize("hasAuthority('PERM_FACTURE_UPDATE') or hasAuthority('PERM_TREASURY_MANAGE')")
  public FactureResponse payer(@PathVariable Long id, @Valid @RequestBody PaiementFactureRequest request) {
    return factureService.enregistrerPaiement(id, request);
  }
}

