package com.fof.client.controller;

import com.fof.client.dto.ClientResponse;
import com.fof.client.dto.CreerClientRequest;
import com.fof.client.dto.ModifierClientRequest;
import com.fof.client.service.ClientService;
import com.fof.facture.dto.EtatPaiementFactureFiltre;
import com.fof.facture.dto.FacturesClientListeResponse;
import com.fof.facture.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clients")
public class ClientController {

  private final ClientService clientService;
  private final FactureService factureService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERM_CLIENT_MANAGE')")
  public ClientResponse creer(@Valid @RequestBody CreerClientRequest request) {
    return clientService.creer(request);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('PERM_CLIENT_MANAGE')")
  public ClientResponse modifier(
      @PathVariable Long id,
      @Valid @RequestPart("donnees") ModifierClientRequest donnees,
      @RequestPart(value = "photo", required = false) MultipartFile photo
  ) {
    return clientService.modifier(id, donnees, photo);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_CLIENT_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public ClientResponse detail(@PathVariable Long id) {
    return clientService.detail(id);
  }

  /**
   * Factures du client (hors annulées) + compteurs payées / impayées. Filtre optionnel {@code etatPaiement}.
   */
  @GetMapping("/{id}/factures")
  @PreAuthorize("hasAuthority('PERM_FACTURE_READ')")
  public FacturesClientListeResponse listerFacturesDuClient(
      @PathVariable Long id,
      String recherche,
      EtatPaiementFactureFiltre etatPaiement,
      @PageableDefault(size = 20, sort = "dateEmission", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return factureService.listerPourClient(id, recherche, etatPaiement, pageable);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_CLIENT_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public Page<ClientResponse> lister(String recherche, @PageableDefault(size = 20) Pageable pageable) {
    return clientService.lister(recherche, pageable);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_CLIENT_MANAGE')")
  public void supprimer(@PathVariable Long id) {
    clientService.supprimer(id);
  }
}

