package com.fof.depense.service;

import com.fof.depense.dto.CreerDepenseRequest;
import com.fof.depense.dto.DepenseResponse;
import com.fof.depense.dto.ModifierDepenseRequest;
import com.fof.depense.dto.PayerDepenseRequest;
import com.fof.depense.entity.Depense;
import com.fof.depense.entity.StatutDepense;
import com.fof.depense.repository.DepenseRepository;
import com.fof.tresorerie.dto.RetraitRequest;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.service.CompteService;
import com.fof.tresorerie.service.TransactionTresorerieService;
import com.fof.audit.service.JournalAuditService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DepenseService {

  private final DepenseRepository depenseRepository;
  private final CompteService compteService;
  private final TransactionTresorerieService transactionTresorerieService;
  private final JournalAuditService journalAuditService;

  @Transactional
  public DepenseResponse creer(CreerDepenseRequest request) {
    Objects.requireNonNull(request, "request");
    Depense depense = new Depense();
    depense.setTitre(request.titre().trim());
    depense.setMontant(request.montant());
    depense.setCategorie(nettoyer(request.categorie()));
    depense.setDateDepense(request.dateDepense() == null ? LocalDate.now() : request.dateDepense());
    depense.setDescription(nettoyer(request.description()));
    Depense sauvegardee = depenseRepository.save(depense);
    journalAuditService.enregistrer("DEPENSE_CREATION", "Depense", sauvegardee.getId(),
        "Création dépense titre=" + sauvegardee.getTitre() + " montant=" + sauvegardee.getMontant());
    return versResponse(sauvegardee);
  }

  @Transactional
  public DepenseResponse modifier(Long id, ModifierDepenseRequest request) {
    Objects.requireNonNull(request, "request");
    Depense depense = charger(id);
    if (depense.getStatut() == StatutDepense.PAYEE) {
      throw new ValidationException("Impossible de modifier une dépense déjà payée");
    }
    depense.setTitre(request.titre().trim());
    depense.setMontant(request.montant());
    depense.setCategorie(nettoyer(request.categorie()));
    depense.setDateDepense(request.dateDepense() == null ? depense.getDateDepense() : request.dateDepense());
    depense.setDescription(nettoyer(request.description()));
    return versResponse(depense);
  }

  @Transactional(readOnly = true)
  public DepenseResponse detail(Long id) {
    return versResponse(charger(id));
  }

  @Transactional(readOnly = true)
  public Page<DepenseResponse> lister(String recherche, StatutDepense statut, LocalDate debut, LocalDate fin, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return depenseRepository.rechercher(q, statut, debut, fin, pageable).map(this::versResponse);
  }

  @Transactional
  public DepenseResponse approuver(Long id) {
    Depense depense = charger(id);
    if (depense.getStatut() != StatutDepense.BROUILLON) {
      throw new ValidationException("Statut invalide pour approbation");
    }
    depense.setStatut(StatutDepense.APPROUVEE);
    journalAuditService.enregistrer("DEPENSE_APPROBATION", "Depense", depense.getId(), "Approbation dépense");
    return versResponse(depense);
  }

  @Transactional
  public DepenseResponse payer(Long id, PayerDepenseRequest request) {
    Objects.requireNonNull(request, "request");
    Depense depense = charger(id);
    if (depense.getStatut() == StatutDepense.ANNULEE) {
      throw new ValidationException("Dépense annulée");
    }
    if (depense.getStatut() == StatutDepense.PAYEE) {
      throw new ValidationException("Dépense déjà payée");
    }
    if (depense.getStatut() != StatutDepense.APPROUVEE) {
      throw new ValidationException("La dépense doit être APPROUVEE avant paiement");
    }

    Compte comptePaiement = compteService.chargerCompte(request.comptePaiementId());
    transactionTresorerieService.retraitAvecReference(new RetraitRequest(
        comptePaiement.getId(),
        depense.getMontant(),
        "Paiement dépense: " + depense.getTitre()
    ), TypeReference.DEPENSE, depense.getId());

    depense.setComptePaiement(comptePaiement);
    depense.setDatePaiement(Instant.now());
    depense.setStatut(StatutDepense.PAYEE);
    journalAuditService.enregistrer("DEPENSE_PAIEMENT", "Depense", depense.getId(),
        "Paiement dépense montant=" + depense.getMontant() + " comptePaiementId=" + comptePaiement.getId());
    return versResponse(depense);
  }

  @Transactional
  public void supprimer(Long id) {
    Depense depense = charger(id);
    if (depense.getStatut() == StatutDepense.PAYEE) {
      throw new ValidationException("Impossible de supprimer une dépense payée");
    }
    depenseRepository.delete(depense);
  }

  private Depense charger(Long id) {
    return depenseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Dépense introuvable: " + id));
  }

  private String nettoyer(String v) {
    if (v == null) {
      return null;
    }
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }

  private DepenseResponse versResponse(Depense d) {
    return new DepenseResponse(
        d.getId(),
        d.getTitre(),
        d.getMontant(),
        d.getCategorie(),
        d.getDateDepense(),
        d.getDescription(),
        d.getStatut(),
        d.getComptePaiement() == null ? null : d.getComptePaiement().getId(),
        d.getDatePaiement(),
        d.getDateCreation()
    );
  }
}

