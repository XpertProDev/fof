package com.fof.depense.controller;

import com.fof.depense.dto.CreerDepenseRequest;
import com.fof.depense.dto.DepenseResponse;
import com.fof.depense.dto.ModifierDepenseRequest;
import com.fof.depense.dto.PayerDepenseRequest;
import com.fof.depense.entity.StatutDepense;
import com.fof.depense.service.DepenseService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/depenses")
public class DepenseController {

  private final DepenseService depenseService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE')")
  public DepenseResponse creer(@Valid @RequestBody CreerDepenseRequest request) {
    return depenseService.creer(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE')")
  public DepenseResponse modifier(@PathVariable Long id, @Valid @RequestBody ModifierDepenseRequest request) {
    return depenseService.modifier(id, request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public DepenseResponse detail(@PathVariable Long id) {
    return depenseService.detail(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public Page<DepenseResponse> lister(
      String recherche,
      StatutDepense statut,
      LocalDate debut,
      LocalDate fin,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return depenseService.lister(recherche, statut, debut, fin, pageable);
  }

  @PostMapping("/{id}/approbation")
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE')")
  public DepenseResponse approuver(@PathVariable Long id) {
    return depenseService.approuver(id);
  }

  @PostMapping("/{id}/paiement")
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE') or hasAuthority('PERM_TREASURY_MANAGE')")
  public DepenseResponse payer(@PathVariable Long id, @Valid @RequestBody PayerDepenseRequest request) {
    return depenseService.payer(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_EXPENSE_MANAGE')")
  public void supprimer(@PathVariable Long id) {
    depenseService.supprimer(id);
  }
}

