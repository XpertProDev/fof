package com.fof.tresorerie.service;

import com.fof.tresorerie.dto.CompteResponse;
import com.fof.tresorerie.dto.CreerCompteRequest;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.repository.CompteRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CompteService {

  private final CompteRepository compteRepository;

  @Transactional
  public CompteResponse creerCompte(CreerCompteRequest request) {
    Objects.requireNonNull(request, "request");
    Compte compte = new Compte();
    compte.setNom(request.nom().trim());
    compte.setType(request.type());
    Compte sauvegarde = compteRepository.save(compte);
    return versResponse(sauvegarde);
  }

  @Transactional(readOnly = true)
  public Page<CompteResponse> listerComptes(String recherche, Pageable pageable) {
    if (recherche == null || recherche.isBlank()) {
      return compteRepository.findAll(pageable).map(this::versResponse);
    }
    return compteRepository.findByNomContainingIgnoreCase(recherche.trim(), pageable).map(this::versResponse);
  }

  @Transactional(readOnly = true)
  public Compte chargerCompte(Long id) {
    return compteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Compte introuvable: " + id));
  }

  private CompteResponse versResponse(Compte compte) {
    return new CompteResponse(compte.getId(), compte.getNom(), compte.getType(), compte.getSoldeActuel());
  }
}

