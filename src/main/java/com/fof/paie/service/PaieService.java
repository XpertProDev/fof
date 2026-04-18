package com.fof.paie.service;

import com.fof.employe.entity.Employe;
import com.fof.employe.service.EmployeService;
import com.fof.paie.dto.CreerPaieRequest;
import com.fof.paie.dto.PaiementPaieResponse;
import com.fof.paie.dto.PaieResponse;
import com.fof.paie.dto.PayerPaieRequest;
import com.fof.paie.entity.Paie;
import com.fof.paie.entity.PaiementPaie;
import com.fof.paie.entity.StatutPaie;
import com.fof.paie.repository.PaieRepository;
import com.fof.paie.repository.PaiementPaieRepository;
import com.fof.tresorerie.dto.RetraitRequest;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.service.CompteService;
import com.fof.tresorerie.service.TransactionTresorerieService;
import com.fof.audit.service.JournalAuditService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaieService {

  private static final int SCALE_ARGENT = 2;

  private final PaieRepository paieRepository;
  private final PaiementPaieRepository paiementPaieRepository;
  private final EmployeService employeService;
  private final CompteService compteService;
  private final TransactionTresorerieService transactionTresorerieService;
  private final JournalAuditService journalAuditService;

  @Transactional
  public PaieResponse creer(CreerPaieRequest request) {
    Objects.requireNonNull(request, "request");
    Employe employe = employeService.charger(request.employeId());

    YearMonth mois = parseMois(request.mois());
    String moisStr = mois.toString();
    if (paieRepository.existsByEmployeIdAndMois(employe.getId(), moisStr)) {
      throw new ValidationException("Paie déjà existante pour cet employé et ce mois");
    }

    BigDecimal primes = request.primes().setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    BigDecimal deductions = request.deductions().setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    BigDecimal salaireBase = employe.getSalaireBase().setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    BigDecimal net = salaireBase.add(primes).subtract(deductions).setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    if (net.signum() < 0) {
      throw new ValidationException("Salaire net invalide (négatif)");
    }

    Paie paie = new Paie();
    paie.setEmploye(employe);
    paie.setMois(moisStr);
    paie.setSalaireBase(salaireBase);
    paie.setPrimes(primes);
    paie.setDeductions(deductions);
    paie.setSalaireNet(net);
    paie.setStatut(StatutPaie.BROUILLON);

    Paie sauvegardee = paieRepository.save(paie);
    journalAuditService.enregistrer("PAIE_CREATION", "Paie", sauvegardee.getId(),
        "Création paie mois=" + sauvegardee.getMois() + " employeId=" + employe.getId() + " net=" + sauvegardee.getSalaireNet());
    return versResponse(sauvegardee, true);
  }

  @Transactional
  public PaieResponse approuver(Long id) {
    Paie paie = charger(id);
    if (paie.getStatut() != StatutPaie.BROUILLON) {
      throw new ValidationException("Statut invalide pour approbation");
    }
    paie.setStatut(StatutPaie.APPROUVEE);
    journalAuditService.enregistrer("PAIE_APPROBATION", "Paie", paie.getId(), "Approbation paie");
    return versResponse(paie, true);
  }

  @Transactional
  public PaieResponse payer(Long id, PayerPaieRequest request) {
    Objects.requireNonNull(request, "request");
    Paie paie = charger(id);
    if (paie.getStatut() == StatutPaie.ANNULEE) {
      throw new ValidationException("Paie annulée");
    }
    if (paie.getStatut() == StatutPaie.PAYEE) {
      throw new ValidationException("Paie déjà payée");
    }
    if (paie.getStatut() != StatutPaie.APPROUVEE) {
      throw new ValidationException("La paie doit être APPROUVEE avant paiement");
    }

    BigDecimal montant = request.montant().setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    if (montant.compareTo(paie.getSalaireNet()) > 0) {
      throw new ValidationException("Le paiement dépasse le salaire net");
    }
    if (montant.signum() <= 0) {
      throw new ValidationException("Montant invalide");
    }

    Compte compteSource = compteService.chargerCompte(request.compteSourceId());

    transactionTresorerieService.retraitAvecReference(new RetraitRequest(
        compteSource.getId(),
        montant,
        "Paiement paie " + paie.getMois() + " - " + paie.getEmploye().getNom() + " " + paie.getEmploye().getPrenom()
    ), TypeReference.PAIE, paie.getId());

    PaiementPaie paiement = new PaiementPaie();
    paiement.setPaie(paie);
    paiement.setMontant(montant);
    paiement.setCompteSource(compteSource);
    paiement.setCommentaire(nettoyer(request.commentaire()));
    paiementPaieRepository.save(paiement);

    // Pour cette étape: un seul paiement -> paie PAYEE si montant == net
    if (montant.compareTo(paie.getSalaireNet()) == 0) {
      paie.setStatut(StatutPaie.PAYEE);
    }

    journalAuditService.enregistrer("PAIE_PAIEMENT", "Paie", paie.getId(),
        "Paiement=" + montant + " compteSourceId=" + compteSource.getId());
    return versResponse(paie, true);
  }

  @Transactional(readOnly = true)
  public PaieResponse detail(Long id) {
    return versResponse(charger(id), true);
  }

  @Transactional(readOnly = true)
  public Page<PaieResponse> lister(Long employeId, StatutPaie statut, String mois, Pageable pageable) {
    String moisStr = (mois == null || mois.isBlank()) ? null : parseMois(mois.trim()).toString();
    return paieRepository.rechercher(employeId, statut, moisStr, pageable).map(p -> versResponse(p, false));
  }

  @Transactional
  public void supprimer(Long id) {
    Paie paie = charger(id);
    if (paie.getStatut() == StatutPaie.PAYEE) {
      throw new ValidationException("Impossible de supprimer une paie payée");
    }
    paieRepository.delete(paie);
  }

  private Paie charger(Long id) {
    return paieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Paie introuvable: " + id));
  }

  private PaieResponse versResponse(Paie p, boolean inclurePaiements) {
    List<PaiementPaieResponse> paiements = inclurePaiements
        ? paiementPaieRepository.findByPaieId(p.getId()).stream()
            .map(pp -> new PaiementPaieResponse(
                pp.getId(),
                pp.getMontant(),
                pp.getDatePaiement(),
                pp.getCompteSource().getId(),
                pp.getCommentaire()))
            .toList()
        : List.of();

    return new PaieResponse(
        p.getId(),
        p.getEmploye().getId(),
        p.getEmploye().getNom() + " " + p.getEmploye().getPrenom(),
        p.getMois(),
        p.getSalaireBase(),
        p.getPrimes(),
        p.getDeductions(),
        p.getSalaireNet(),
        p.getStatut(),
        p.getDateCreation(),
        paiements
    );
  }

  private YearMonth parseMois(String mois) {
    try {
      return YearMonth.parse(mois);
    } catch (Exception e) {
      throw new ValidationException("Mois invalide (format attendu: YYYY-MM)");
    }
  }

  private String nettoyer(String v) {
    if (v == null) {
      return null;
    }
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }
}

