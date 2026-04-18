package com.fof.paie.controller;

import com.fof.paie.dto.CreerPaieRequest;
import com.fof.paie.dto.PaieResponse;
import com.fof.paie.dto.PayerPaieRequest;
import com.fof.paie.entity.StatutPaie;
import com.fof.paie.service.PaieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/paies")
public class PaieController {

  private final PaieService paieService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE')")
  public PaieResponse creer(@Valid @RequestBody CreerPaieRequest request) {
    return paieService.creer(request);
  }

  @PostMapping("/{id}/approbation")
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE')")
  public PaieResponse approuver(@PathVariable Long id) {
    return paieService.approuver(id);
  }

  @PostMapping("/{id}/paiement")
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE') or hasAuthority('PERM_TREASURY_MANAGE')")
  public PaieResponse payer(@PathVariable Long id, @Valid @RequestBody PayerPaieRequest request) {
    return paieService.payer(id, request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE')")
  public PaieResponse detail(@PathVariable Long id) {
    return paieService.detail(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE')")
  public Page<PaieResponse> lister(
      Long employeId,
      StatutPaie statut,
      String mois,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return paieService.lister(employeId, statut, mois, pageable);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_PAYROLL_MANAGE')")
  public void supprimer(@PathVariable Long id) {
    paieService.supprimer(id);
  }
}

