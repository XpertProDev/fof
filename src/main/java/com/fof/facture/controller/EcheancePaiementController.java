package com.fof.facture.controller;

import com.fof.facture.dto.EcheancePaiementResponse;
import com.fof.facture.dto.PlanPaiementRequest;
import com.fof.facture.service.EcheancePaiementService;
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
@RequestMapping("/api/factures/{factureId}/echeances")
public class EcheancePaiementController {

  private final EcheancePaiementService echeancePaiementService;

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_FACTURE_READ')")
  public Page<EcheancePaiementResponse> lister(
      @PathVariable Long factureId,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return echeancePaiementService.listerParFacture(factureId, pageable);
  }

  @PostMapping("/plan")
  @PreAuthorize("hasAuthority('PERM_FACTURE_UPDATE')")
  public void definirPlan(@PathVariable Long factureId, @Valid @RequestBody PlanPaiementRequest request) {
    echeancePaiementService.definirPlanPaiement(factureId, request);
  }
}

