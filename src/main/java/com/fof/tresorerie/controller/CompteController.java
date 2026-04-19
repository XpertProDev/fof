package com.fof.tresorerie.controller;

import com.fof.tresorerie.dto.CompteResponse;
import com.fof.tresorerie.dto.CreerCompteRequest;
import com.fof.tresorerie.service.CompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comptes")
public class CompteController {

  private final CompteService compteService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE')")
  public CompteResponse creer(@Valid @RequestBody CreerCompteRequest request) {
    return compteService.creerCompte(request);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_ACCOUNTING_VIEW') or hasAuthority('PERM_TREASURY_MANAGE')")
  public Page<CompteResponse> lister(
      String recherche,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return compteService.listerComptes(recherche, pageable);
  }
}

