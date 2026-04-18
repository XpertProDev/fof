package com.fof.facture.service;

import com.fof.audit.service.JournalAuditService;
import com.fof.facture.dto.EcheancePaiementResponse;
import com.fof.facture.dto.PlanPaiementRequest;
import com.fof.facture.entity.EcheancePaiement;
import com.fof.facture.entity.Facture;
import com.fof.facture.entity.StatutEcheancePaiement;
import com.fof.facture.repository.FactureRepository;
import com.fof.facture.repository.EcheancePaiementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EcheancePaiementService {

  private static final int SCALE_ARGENT = 2;

  private final EcheancePaiementRepository echeanceRepository;
  private final FactureRepository factureRepository;
  private final JournalAuditService journalAuditService;

  @Transactional
  public Page<EcheancePaiementResponse> listerParFacture(Long factureId, Pageable pageable) {
    return echeanceRepository.findByFactureId(factureId, pageable).map(this::versResponse);
  }

  @Transactional
  public void definirPlanPaiement(Long factureId, PlanPaiementRequest request) {
    Objects.requireNonNull(request, "request");
    Facture facture = factureRepository.findById(factureId)
        .orElseThrow(() -> new EntityNotFoundException("Facture introuvable: " + factureId));

    // Nettoie et remplace le plan
    facture.getEcheancesPaiement().clear();

    var echeancesTriees = request.echeances().stream()
        .sorted(Comparator.comparing(e -> e.datePrevue()))
        .toList();

    BigDecimal totalProgramme = BigDecimal.ZERO;
    for (var e : echeancesTriees) {
      BigDecimal mp = e.montantProgramme().setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
      if (mp.signum() <= 0) throw new ValidationException("Montant programmé invalide");
      if (e.datePrevue() == null) throw new ValidationException("Date prévue obligatoire");

      EcheancePaiement ep = new EcheancePaiement();
      ep.setFacture(facture);
      ep.setMontantProgramme(mp);
      ep.setMontantPaye(BigDecimal.ZERO.setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
      ep.setDatePrevue(e.datePrevue());
      ep.setCommentaire(nettoyer(e.commentaire()));
      ep.setStatut(StatutEcheancePaiement.EN_ATTENTE);
      facture.getEcheancesPaiement().add(ep);
      totalProgramme = totalProgramme.add(mp);
    }

    // Autoriser <= totalTtc (certaines boîtes planifient partiellement)
    if (totalProgramme.compareTo(facture.getTotalTtc()) > 0) {
      throw new ValidationException("Le total des échéances dépasse le total TTC de la facture");
    }

    journalAuditService.enregistrer("FACTURE_PLAN_PAIEMENT", "Facture", facture.getId(),
        "Plan défini: nb=" + facture.getEcheancesPaiement().size() + " totalProgramme=" + totalProgramme);
  }

  @Transactional
  public void repartirPaiementSurEcheances(Long factureId, BigDecimal montantPaiement) {
    if (montantPaiement == null || montantPaiement.signum() <= 0) return;

    var echeances = echeanceRepository.findByFactureIdAndStatutOrderByDatePrevueAsc(factureId, StatutEcheancePaiement.EN_ATTENTE);
    if (echeances.isEmpty()) return;

    BigDecimal reste = montantPaiement.setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    for (EcheancePaiement e : echeances) {
      if (reste.signum() <= 0) break;
      BigDecimal besoin = e.getMontantProgramme().subtract(e.getMontantPaye()).max(BigDecimal.ZERO);
      if (besoin.signum() <= 0) {
        e.setStatut(StatutEcheancePaiement.PAYEE);
        continue;
      }
      BigDecimal applique = reste.min(besoin);
      e.setMontantPaye(e.getMontantPaye().add(applique).setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
      reste = reste.subtract(applique).setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
      if (e.getMontantPaye().compareTo(e.getMontantProgramme()) >= 0) {
        e.setStatut(StatutEcheancePaiement.PAYEE);
        e.setDatePaiement(Instant.now());
      }
    }
  }

  @Transactional
  public void marquerEcheancesManqueesSiBesoin() {
    LocalDate aujourdhui = LocalDate.now();
    var echeances = echeanceRepository.findByStatutAndDatePrevueBefore(StatutEcheancePaiement.EN_ATTENTE, aujourdhui);
    for (EcheancePaiement e : echeances) {
      e.setStatut(StatutEcheancePaiement.MANQUEE);
    }
  }

  private EcheancePaiementResponse versResponse(EcheancePaiement e) {
    return new EcheancePaiementResponse(
        e.getId(),
        e.getFacture().getId(),
        e.getMontantProgramme(),
        e.getMontantPaye(),
        e.getDatePrevue(),
        e.getStatut(),
        e.getCommentaire(),
        e.getDatePaiement()
    );
  }

  private String nettoyer(String v) {
    if (v == null) return null;
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }
}

