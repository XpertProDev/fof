package com.fof.employe.service;

import com.fof.employe.dto.CreerEmployeRequest;
import com.fof.employe.dto.EmployeResponse;
import com.fof.employe.dto.ModifierEmployeRequest;
import com.fof.employe.entity.Employe;
import com.fof.employe.entity.StatutEmploye;
import com.fof.employe.repository.EmployeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EmployeService {

  private final EmployeRepository employeRepository;

  @Transactional
  public EmployeResponse creer(CreerEmployeRequest request) {
    Objects.requireNonNull(request, "request");
    Employe e = new Employe();
    appliquer(e, request.nom(), request.prenom(), request.telephone(), request.fonction(), request.salaireBase(),
        request.typeContrat(), request.dateEmbauche(), request.statut());
    Employe sauvegarde = employeRepository.save(e);
    return versResponse(sauvegarde);
  }

  @Transactional
  public EmployeResponse modifier(Long id, ModifierEmployeRequest request) {
    Objects.requireNonNull(request, "request");
    Employe e = charger(id);
    appliquer(e, request.nom(), request.prenom(), request.telephone(), request.fonction(), request.salaireBase(),
        request.typeContrat(), request.dateEmbauche(), request.statut());
    return versResponse(e);
  }

  @Transactional(readOnly = true)
  public EmployeResponse detail(Long id) {
    return versResponse(charger(id));
  }

  @Transactional(readOnly = true)
  public Page<EmployeResponse> lister(String recherche, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return employeRepository.rechercher(q, pageable).map(this::versResponse);
  }

  @Transactional
  public void supprimer(Long id) {
    if (!employeRepository.existsById(id)) {
      throw new EntityNotFoundException("Employé introuvable: " + id);
    }
    employeRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Employe charger(Long id) {
    return employeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employé introuvable: " + id));
  }

  private void appliquer(
      Employe e,
      String nom,
      String prenom,
      String telephone,
      String fonction,
      java.math.BigDecimal salaireBase,
      String typeContrat,
      LocalDate dateEmbauche,
      StatutEmploye statut
  ) {
    e.setNom(nom.trim());
    e.setPrenom(prenom.trim());
    e.setTelephone(nettoyer(telephone));
    e.setFonction(nettoyer(fonction));
    e.setSalaireBase(salaireBase);
    e.setTypeContrat(nettoyer(typeContrat));
    if (dateEmbauche != null) {
      e.setDateEmbauche(dateEmbauche);
    }
    e.setStatut(statut == null ? StatutEmploye.ACTIF : statut);
  }

  private String nettoyer(String v) {
    if (v == null) {
      return null;
    }
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }

  private EmployeResponse versResponse(Employe e) {
    return new EmployeResponse(
        e.getId(),
        e.getNom(),
        e.getPrenom(),
        e.getTelephone(),
        e.getFonction(),
        e.getSalaireBase(),
        e.getTypeContrat(),
        e.getPhotoUrl(),
        e.getDateEmbauche(),
        e.getStatut(),
        e.getDateCreation()
    );
  }
}

