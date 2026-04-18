package com.fof.notification.service;

import com.fof.facture.entity.Facture;
import com.fof.facture.entity.StatutFacture;
import com.fof.facture.repository.FactureRepository;
import com.fof.facture.entity.EcheancePaiement;
import com.fof.facture.entity.StatutEcheancePaiement;
import com.fof.facture.repository.EcheancePaiementRepository;
import com.fof.notification.entity.TypeNotification;
import com.fof.paie.entity.Paie;
import com.fof.paie.entity.StatutPaie;
import com.fof.paie.repository.PaieRepository;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.repository.CompteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlertePlanifieeService {

  private final FactureRepository factureRepository;
  private final EcheancePaiementRepository echeancePaiementRepository;
  private final PaieRepository paieRepository;
  private final CompteRepository compteRepository;
  private final NotificationService notificationService;

  @Value("${app.alertes.facture.echeance-jours:3}")
  private int echeanceJours;

  @Value("${app.alertes.compte.solde-faible-seuil:0.00}")
  private BigDecimal seuilSoldeFaible;

  /** Factures encore impayées (y compris anciennes en BROUILLON tant qu’il reste à payer). */
  private static final List<StatutFacture> STATUTS_A_SURVEILLER = List.of(
      StatutFacture.ENVOYEE,
      StatutFacture.PARTIELLEMENT_PAYEE,
      StatutFacture.EN_RETARD,
      StatutFacture.BROUILLON
  );

  @Scheduled(cron = "${app.alertes.cron:0 */10 * * * *}")
  @Transactional
  public void executer() {
    alerterEcheancesPaiement();
    marquerEcheancesManquees();
    alerterEcheancesFactures();
    alerterFacturesEnRetard();
    alerterPaiesApprouvees();
    alerterSoldeFaible();
  }

  private void alerterEcheancesPaiement() {
    LocalDate aujourdHui = LocalDate.now();
    LocalDate fin = aujourdHui.plusDays(Math.max(0, echeanceJours));
    List<EcheancePaiement> echeances = echeancePaiementRepository.findByStatutAndDatePrevueBetween(
        StatutEcheancePaiement.EN_ATTENTE, aujourdHui, fin);
    for (EcheancePaiement e : echeances) {
      String cle = "ECHEANCE:" + e.getDatePrevue();
      notificationService.creerSiAbsente(
          TypeNotification.ECHEANCE_PAIEMENT_IMMINENTE,
          "Échéance paiement facture " + e.getFacture().getNumero() + " (" + e.getDatePrevue() + "), programmé " + e.getMontantProgramme()
              + ", payé " + e.getMontantPaye(),
          TypeReference.FACTURE,
          e.getFacture().getId(),
          cle
      );
    }
  }

  private void marquerEcheancesManquees() {
    LocalDate aujourdHui = LocalDate.now();
    List<EcheancePaiement> echeances = echeancePaiementRepository.findByStatutAndDatePrevueBefore(
        StatutEcheancePaiement.EN_ATTENTE, aujourdHui);
    for (EcheancePaiement e : echeances) {
      e.setStatut(StatutEcheancePaiement.MANQUEE);
      String cle = "MANQUEE:" + e.getDatePrevue();
      notificationService.creerSiAbsente(
          TypeNotification.ECHEANCE_PAIEMENT_MANQUEE,
          "Échéance manquée facture " + e.getFacture().getNumero() + " (prévue " + e.getDatePrevue() + "), programmé "
              + e.getMontantProgramme() + ", payé " + e.getMontantPaye(),
          TypeReference.FACTURE,
          e.getFacture().getId(),
          cle
      );
    }
  }

  private void alerterEcheancesFactures() {
    LocalDate aujourdHui = LocalDate.now();
    LocalDate fin = aujourdHui.plusDays(Math.max(0, echeanceJours));
    List<Facture> factures = factureRepository.findByStatutInAndDateEcheanceBetween(STATUTS_A_SURVEILLER, aujourdHui, fin);
    for (Facture f : factures) {
      if (f.getDateEcheance() == null) continue;
      if (!doitAlerterEcheanceFacture(f)) {
        continue;
      }
      String cle = "ECHEANCE:" + f.getDateEcheance();
      notificationService.creerSiAbsente(
          TypeNotification.FACTURE_ECHEANCE_PROCHE,
          "Facture " + f.getNumero() + " proche échéance (" + f.getDateEcheance() + "), restant " + f.getMontantRestant(),
          TypeReference.FACTURE,
          f.getId(),
          cle
      );
    }
  }

  private void alerterFacturesEnRetard() {
    LocalDate aujourdHui = LocalDate.now();
    List<Facture> factures = factureRepository.findByStatutInAndDateEcheanceBefore(STATUTS_A_SURVEILLER, aujourdHui);
    for (Facture f : factures) {
      if (f.getDateEcheance() == null) continue;
      if (!doitAlerterEcheanceFacture(f)) {
        continue;
      }
      String cle = "RETARD:" + f.getDateEcheance();
      notificationService.creerSiAbsente(
          TypeNotification.FACTURE_EN_RETARD,
          "Facture " + f.getNumero() + " en retard (échéance " + f.getDateEcheance() + "), restant " + f.getMontantRestant(),
          TypeReference.FACTURE,
          f.getId(),
          cle
      );
      // Option: on peut aussi forcer statut EN_RETARD si pas soldée
      if (f.getStatut() != StatutFacture.EN_RETARD && f.getStatut() != StatutFacture.PAYEE && f.getStatut() != StatutFacture.ANNULEE) {
        f.setStatut(StatutFacture.EN_RETARD);
      }
    }
  }

  /**
   * Brouillon : dès qu’une {@code dateEcheance} est renseignée, on alerte (même sans reste à payer encore saisi).
   * Autres statuts : uniquement s’il reste quelque chose à payer.
   */
  private boolean doitAlerterEcheanceFacture(Facture f) {
    if (f.getStatut() == StatutFacture.BROUILLON) {
      return true;
    }
    return f.getMontantRestant() != null && f.getMontantRestant().compareTo(BigDecimal.ZERO) > 0;
  }

  private void alerterPaiesApprouvees() {
    List<Paie> paies = paieRepository.findByStatut(StatutPaie.APPROUVEE);
    for (Paie p : paies) {
      String cle = "PAIE:" + p.getMois();
      notificationService.creerSiAbsente(
          TypeNotification.PAIE_A_PAYER,
          "Paie à payer: " + p.getEmploye().getNom() + " " + p.getEmploye().getPrenom() + " (" + p.getMois() + "), net " + p.getSalaireNet(),
          TypeReference.PAIE,
          p.getId(),
          cle
      );
    }
  }

  private void alerterSoldeFaible() {
    if (seuilSoldeFaible == null) return;
    List<Compte> comptes = compteRepository.findBySoldeActuelLessThan(seuilSoldeFaible);
    for (Compte c : comptes) {
      String cle = "SOLDE_FAIBLE:" + LocalDate.now();
      notificationService.creerSiAbsente(
          TypeNotification.SOLDE_FAIBLE,
          "Solde faible sur compte " + c.getNom() + " (" + c.getType() + "): " + c.getSoldeActuel(),
          TypeReference.MANUEL,
          c.getId(),
          cle
      );
    }
  }
}

