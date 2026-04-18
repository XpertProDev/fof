package com.fof.facture.service;

import com.fof.client.entity.Client;
import com.fof.client.service.ClientService;
import com.fof.facture.dto.CreerFactureAvecPaiementRequest;
import com.fof.facture.dto.CompteursFacturesClientResponse;
import com.fof.facture.dto.CreerFactureRequest;
import com.fof.facture.dto.CreerLigneFactureRequest;
import com.fof.facture.dto.EtatPaiementFactureFiltre;
import com.fof.facture.dto.FactureResponse;
import com.fof.facture.dto.FacturesClientListeResponse;
import com.fof.facture.dto.LigneFactureResponse;
import com.fof.facture.dto.PaiementFactureRequest;
import com.fof.facture.dto.PaiementFactureResponse;
import com.fof.facture.entity.Facture;
import com.fof.facture.entity.LigneFacture;
import com.fof.facture.entity.ModeTva;
import com.fof.facture.entity.PaiementFacture;
import com.fof.facture.entity.StatutFacture;
import com.fof.facture.repository.FactureRepository;
import com.fof.facture.repository.PaiementFactureRepository;
import com.fof.tresorerie.dto.DepotRequest;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.service.CompteService;
import com.fof.tresorerie.service.TransactionTresorerieService;
import com.fof.audit.service.JournalAuditService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FactureService {

  private static final int SCALE_ARGENT = 2;

  private final FactureRepository factureRepository;
  private final PaiementFactureRepository paiementRepository;
  private final ClientService clientService;
  private final ParametreTvaService parametreTvaService;
  private final CompteService compteService;
  private final TransactionTresorerieService transactionTresorerieService;
  private final JournalAuditService journalAuditService;
  private final EcheancePaiementService echeancePaiementService;

  @Transactional
  public FactureResponse creer(CreerFactureRequest request) {
    Objects.requireNonNull(request, "request");

    Client client = clientService.charger(request.clientId());

    Facture facture = new Facture();
    facture.setClient(client);
    facture.setDateEmission(request.dateEmission() == null ? LocalDate.now() : request.dateEmission());
    facture.setDateEcheance(request.dateEcheance());
    facture.setModeTva(request.modeTva());

    BigDecimal tauxTva = resoudreTauxTva(request.modeTva(), request.tauxTva());
    facture.setTauxTva(tauxTva);

    for (CreerLigneFactureRequest lr : request.lignes()) {
      LigneFacture ligne = new LigneFacture();
      ligne.setFacture(facture);
      ligne.setDescription(lr.description().trim());
      ligne.setQuantite(lr.quantite().setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
      ligne.setPrixUnitaire(lr.prixUnitaire().setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
      BigDecimal total = ligne.getQuantite().multiply(ligne.getPrixUnitaire()).setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
      ligne.setTotalLigne(total);
      facture.getLignes().add(ligne);
    }

    recalculerTotaux(facture);
    // À la création, une facture avec un reste à payer est considérée comme émise (ENVOYEE / EN_RETARD), pas brouillon.
    mettreAJourStatutPaiement(facture);

    Facture sauvegardee = factureRepository.save(facture);
    // Numéro unique simple (sera amélioré plus tard: séquence par année/exercice)
    sauvegardee.setNumero("N°" + sauvegardee.getDateEmission().getYear() + "-" + String.format("%06d", sauvegardee.getId()));
    // Re-synchronise le statut après attribution du numéro (évite tout état « brouillon » résiduel en base).
    mettreAJourStatutPaiement(sauvegardee);
    factureRepository.save(sauvegardee);
    journalAuditService.enregistrer(
        "FACTURE_CREATION",
        "Facture",
        sauvegardee.getId(),
        "Création facture " + sauvegardee.getNumero() + " clientId=" + client.getId() + " totalTtc=" + sauvegardee.getTotalTtc()
    );
    return versResponse(sauvegardee, true);
  }

  /**
   * Crée une facture et enregistre un paiement initial optionnel dans la même requête.
   *
   * <p>Si {@code paiement} est absent → comportement identique à {@link #creer(CreerFactureRequest)}.
   */
  @Transactional
  public FactureResponse creerAvecPaiement(CreerFactureAvecPaiementRequest request) {
    Objects.requireNonNull(request, "request");

    FactureResponse creee = creer(new CreerFactureRequest(
        request.clientId(),
        request.dateEmission(),
        request.dateEcheance(),
        request.modeTva(),
        request.tauxTva(),
        request.lignes()
    ));

    if (request.paiement() == null) {
      return creee;
    }
    return enregistrerPaiement(creee.id(), request.paiement());
  }

  @Transactional(readOnly = true)
  public FactureResponse detail(Long id) {
    Facture facture = charger(id);
    return versResponse(facture, true);
  }

  @Transactional(readOnly = true)
  public Page<FactureResponse> lister(String recherche, Long clientId, StatutFacture statut, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return factureRepository.rechercher(q, clientId, statut, pageable).map(f -> versResponse(f, false));
  }

  /**
   * Liste paginée des factures d’un client (hors annulées) avec compteurs payées / impayées.
   *
   * <p>Payée = {@code montantRestant = 0}. Impayée = reste à payer ({@code montantRestant > 0}), hors brouillon.
   */
  @Transactional(readOnly = true)
  public FacturesClientListeResponse listerPourClient(
      Long clientId,
      String recherche,
      EtatPaiementFactureFiltre etatPaiement,
      Pageable pageable
  ) {
    clientService.charger(clientId);
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    EtatPaiementFactureFiltre filtre = etatPaiement == null ? EtatPaiementFactureFiltre.TOUTES : etatPaiement;
    Boolean filtrePayee =
        switch (filtre) {
          case TOUTES -> null;
          case PAYEES -> Boolean.TRUE;
          case IMPAYEES -> Boolean.FALSE;
        };

    long nombrePayees = factureRepository.compterFacturesPayeesPourClient(clientId);
    long nombreImpayees = factureRepository.compterFacturesImpayeesPourClient(clientId);
    long nombreTotal = factureRepository.compterFacturesPourClientHorsAnnulees(clientId);
    CompteursFacturesClientResponse compteurs =
        new CompteursFacturesClientResponse(nombrePayees, nombreImpayees, nombreTotal);

    Page<FactureResponse> page =
        factureRepository.rechercherPourClient(clientId, q, filtrePayee, pageable).map(f -> versResponse(f, false));
    return FacturesClientListeResponse.of(compteurs, page);
  }

  @Transactional
  public FactureResponse enregistrerPaiement(Long factureId, PaiementFactureRequest request) {
    Objects.requireNonNull(request, "request");
    Facture facture = charger(factureId);
    if (facture.getStatut() == StatutFacture.ANNULEE) {
      throw new ValidationException("Facture annulée");
    }

    BigDecimal montant = request.montant();
    if (montant == null || montant.signum() <= 0) {
      throw new ValidationException("Montant invalide");
    }

    BigDecimal restant = facture.getMontantRestant();
    if (restant.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException("Facture déjà soldée");
    }
    if (montant.compareTo(restant) > 0) {
      throw new ValidationException("Le paiement dépasse le montant restant");
    }

    Compte compteDestination = compteService.chargerCompte(request.compteDestinationId());

    PaiementFacture paiement = new PaiementFacture();
    paiement.setFacture(facture);
    paiement.setMontant(montant.setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
    paiement.setCompteDestination(compteDestination);
    paiement.setReference(nettoyer(request.reference()));
    paiement.setCommentaire(nettoyer(request.commentaire()));
    PaiementFacture sauvegardePaiement = paiementRepository.save(paiement);

    // Mouvement trésorerie (entrée) rattaché à la facture
    DepotRequest depot = new DepotRequest(compteDestination.getId(), paiement.getMontant(),
        "Paiement facture " + facture.getNumero() + (paiement.getReference() == null ? "" : (" (" + paiement.getReference() + ")")));
    transactionTresorerieService.depotAvecReference(depot, TypeReference.FACTURE, facture.getId());

    facture.setMontantPaye(facture.getMontantPaye().add(paiement.getMontant()));
    facture.setMontantRestant(facture.getTotalTtc().subtract(facture.getMontantPaye()).max(BigDecimal.ZERO).setScale(SCALE_ARGENT, RoundingMode.HALF_UP));

    echeancePaiementService.repartirPaiementSurEcheances(facture.getId(), paiement.getMontant());

    mettreAJourStatutPaiement(facture);
    journalAuditService.enregistrer(
        "FACTURE_PAIEMENT",
        "Facture",
        facture.getId(),
        "Paiement=" + paiement.getMontant() + " compteDestinationId=" + compteDestination.getId() + " paiementId=" + sauvegardePaiement.getId()
    );
    return versResponse(facture, true);
  }

  private void mettreAJourStatutPaiement(Facture facture) {
    if (facture.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
      facture.setStatut(StatutFacture.PAYEE);
      return;
    }
    if (facture.getMontantPaye().compareTo(BigDecimal.ZERO) > 0) {
      facture.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
      return;
    }
    if (facture.getDateEcheance() != null && facture.getDateEcheance().isBefore(LocalDate.now())) {
      facture.setStatut(StatutFacture.EN_RETARD);
      return;
    }
    facture.setStatut(StatutFacture.ENVOYEE);
  }

  private void recalculerTotaux(Facture facture) {
    BigDecimal totalHt = facture.getLignes().stream()
        .map(LigneFacture::getTotalLigne)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    facture.setTotalHt(totalHt);

    BigDecimal tauxTva = facture.getTauxTva();
    BigDecimal montantTva = (tauxTva == null ? BigDecimal.ZERO : totalHt.multiply(tauxTva))
        .setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    facture.setMontantTva(montantTva);

    BigDecimal totalTtc = totalHt.add(montantTva).setScale(SCALE_ARGENT, RoundingMode.HALF_UP);
    facture.setTotalTtc(totalTtc);

    facture.setMontantPaye(BigDecimal.ZERO.setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
    facture.setMontantRestant(totalTtc.setScale(SCALE_ARGENT, RoundingMode.HALF_UP));
  }

  private BigDecimal resoudreTauxTva(ModeTva modeTva, BigDecimal tauxDemande) {
    if (modeTva == null) {
      throw new ValidationException("Mode TVA obligatoire");
    }
    return switch (modeTva) {
      case DESACTIVEE -> null;
      case PAR_DEFAUT -> parametreTvaService.isTvaActivee() ? normaliserTaux(parametreTvaService.getTauxTvaParDefaut()) : null;
      case PERSONNALISEE -> normaliserTaux(tauxDemande);
    };
  }

  private BigDecimal normaliserTaux(BigDecimal taux) {
    if (taux == null) {
      throw new ValidationException("Taux TVA obligatoire");
    }
    if (taux.signum() < 0) {
      throw new ValidationException("Taux TVA invalide");
    }
    // ex: 0.18, 0.2...
    return taux.setScale(4, RoundingMode.HALF_UP);
  }

  private String nettoyer(String valeur) {
    if (valeur == null) {
      return null;
    }
    String v = valeur.trim();
    return v.isEmpty() ? null : v;
  }

  Facture chargerEntite(Long id) {
    return factureRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Facture introuvable: " + id));
  }

  private Facture charger(Long id) {
    return chargerEntite(id);
  }

  private FactureResponse versResponse(Facture f, boolean inclureDetails) {
    List<LigneFactureResponse> lignes = inclureDetails
        ? f.getLignes().stream()
        .map(l -> new LigneFactureResponse(l.getId(), l.getDescription(), l.getQuantite(), l.getPrixUnitaire(), l.getTotalLigne()))
        .toList()
        : List.of();

    List<PaiementFactureResponse> paiements = inclureDetails
        ? f.getPaiements().stream()
        .map(p -> new PaiementFactureResponse(
            p.getId(),
            p.getMontant(),
            p.getDatePaiement(),
            p.getCompteDestination() == null ? null : p.getCompteDestination().getId(),
            p.getReference(),
            p.getCommentaire()))
        .toList()
        : List.of();

    return new FactureResponse(
        f.getId(),
        f.getNumero(),
        f.getClient().getId(),
        f.getClient().getNomComplet(),
        f.getDateEmission(),
        f.getDateEcheance(),
        f.getStatut(),
        f.getModeTva(),
        f.getTauxTva(),
        f.getTotalHt(),
        f.getMontantTva(),
        f.getTotalTtc(),
        f.getMontantPaye(),
        f.getMontantRestant(),
        lignes,
        paiements
    );
  }
}

