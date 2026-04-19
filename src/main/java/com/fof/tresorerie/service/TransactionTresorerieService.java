package com.fof.tresorerie.service;

import com.fof.tresorerie.dto.DepotRequest;
import com.fof.tresorerie.dto.FluxMoisRapportResponse;
import com.fof.tresorerie.dto.RetraitRequest;
import com.fof.tresorerie.dto.TransactionTresorerieResponse;
import com.fof.tresorerie.dto.TransfertRequest;
import com.fof.tresorerie.entity.Compte;
import com.fof.tresorerie.entity.StatutTransactionTresorerie;
import com.fof.tresorerie.entity.TransactionTresorerie;
import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.entity.TypeTransaction;
import com.fof.tresorerie.repository.TransactionTresorerieRepository;
import com.fof.tresorerie.repository.TransactionTresorerieSpecs;
import com.fof.audit.service.JournalAuditService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TransactionTresorerieService {

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private final CompteService compteService;
  private final TransactionTresorerieRepository transactionRepository;
  private final JournalAuditService journalAuditService;

  @Value("${app.devise:XOF}")
  private String devise;

  @Transactional
  public TransactionTresorerieResponse depot(DepotRequest request) {
    return depotAvecReference(request, TypeReference.MANUEL, null);
  }

  @Transactional
  public TransactionTresorerieResponse depotAvecReference(DepotRequest request, TypeReference typeReference, Long idReference) {
    Objects.requireNonNull(request, "request");
    Compte compteDestination = compteService.chargerCompte(request.compteDestinationId());
    BigDecimal montant = request.montant();
    if (montant == null || montant.signum() <= 0) {
      throw new ValidationException("Montant invalide");
    }

    compteDestination.setSoldeActuel(compteDestination.getSoldeActuel().add(montant));

    TransactionTresorerie tx = new TransactionTresorerie();
    tx.setType(TypeTransaction.ENTREE);
    tx.setMontant(montant);
    tx.setCompteDestination(compteDestination);
    tx.setTypeReference(typeReference == null ? TypeReference.MANUEL : typeReference);
    tx.setIdReference(idReference);
    tx.setDescription(normaliserDescription(request.description()));

    TransactionTresorerie sauvegarde = transactionRepository.save(tx);
    journalAuditService.enregistrer("TRESORERIE_DEPOT", "TransactionTresorerie", sauvegarde.getId(),
        "Depot montant=" + montant + " compteDestinationId=" + compteDestination.getId());
    return versResponse(sauvegarde);
  }

  @Transactional
  public TransactionTresorerieResponse retrait(RetraitRequest request) {
    return retraitAvecReference(request, TypeReference.MANUEL, null);
  }

  @Transactional
  public TransactionTresorerieResponse retraitAvecReference(RetraitRequest request, TypeReference typeReference, Long idReference) {
    Objects.requireNonNull(request, "request");
    Compte compteSource = compteService.chargerCompte(request.compteSourceId());
    BigDecimal montant = request.montant();
    if (montant == null || montant.signum() <= 0) {
      throw new ValidationException("Montant invalide");
    }
    if (compteSource.getSoldeActuel().compareTo(montant) < 0) {
      throw new ValidationException("Solde insuffisant");
    }

    compteSource.setSoldeActuel(compteSource.getSoldeActuel().subtract(montant));

    TransactionTresorerie tx = new TransactionTresorerie();
    tx.setType(TypeTransaction.SORTIE);
    tx.setMontant(montant);
    tx.setCompteSource(compteSource);
    tx.setTypeReference(typeReference == null ? TypeReference.MANUEL : typeReference);
    tx.setIdReference(idReference);
    tx.setDescription(normaliserDescription(request.description()));

    TransactionTresorerie sauvegarde = transactionRepository.save(tx);
    journalAuditService.enregistrer("TRESORERIE_RETRAIT", "TransactionTresorerie", sauvegarde.getId(),
        "Retrait montant=" + montant + " compteSourceId=" + compteSource.getId());
    return versResponse(sauvegarde);
  }

  @Transactional
  public TransactionTresorerieResponse transfert(TransfertRequest request) {
    Objects.requireNonNull(request, "request");
    if (Objects.equals(request.compteSourceId(), request.compteDestinationId())) {
      throw new ValidationException("Le compte source et destination doivent être différents");
    }

    Compte compteSource = compteService.chargerCompte(request.compteSourceId());
    Compte compteDestination = compteService.chargerCompte(request.compteDestinationId());
    BigDecimal montant = request.montant();

    if (montant == null || montant.signum() <= 0) {
      throw new ValidationException("Montant invalide");
    }
    if (compteSource.getSoldeActuel().compareTo(montant) < 0) {
      throw new ValidationException("Solde insuffisant");
    }

    compteSource.setSoldeActuel(compteSource.getSoldeActuel().subtract(montant));
    compteDestination.setSoldeActuel(compteDestination.getSoldeActuel().add(montant));

    TransactionTresorerie tx = new TransactionTresorerie();
    tx.setType(TypeTransaction.TRANSFERT);
    tx.setMontant(montant);
    tx.setCompteSource(compteSource);
    tx.setCompteDestination(compteDestination);
    tx.setTypeReference(TypeReference.MANUEL);
    tx.setDescription(normaliserDescription(request.description()));

    TransactionTresorerie sauvegarde = transactionRepository.save(tx);
    journalAuditService.enregistrer("TRESORERIE_TRANSFERT", "TransactionTresorerie", sauvegarde.getId(),
        "Transfert montant=" + montant + " sourceId=" + compteSource.getId() + " destinationId=" + compteDestination.getId());
    return versResponse(sauvegarde);
  }

  @Transactional
  public TransactionTresorerieResponse annuler(Long transactionId, String motif) {
    TransactionTresorerie originale = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new EntityNotFoundException("Transaction introuvable: " + transactionId));
    if (originale.getStatut() == StatutTransactionTresorerie.ANNULEE) {
      throw new ValidationException("Transaction déjà annulée");
    }

    // Écriture inverse + ajustement des soldes
    TransactionTresorerie inverse = new TransactionTresorerie();
    inverse.setType(originale.getType());
    inverse.setMontant(originale.getMontant());
    inverse.setTypeReference(originale.getTypeReference());
    inverse.setIdReference(originale.getIdReference());
    inverse.setTransactionOrigine(originale);
    inverse.setMotifAnnulation(normaliserDescription(motif));
    inverse.setDescription("Annulation tx #" + originale.getId() + (motif == null || motif.isBlank() ? "" : (": " + motif.trim())));

    if (originale.getType() == TypeTransaction.ENTREE) {
      // Annuler une entrée = sortie du compte destination
      Compte dest = originale.getCompteDestination();
      if (dest == null) throw new ValidationException("Transaction invalide (compte destination manquant)");
      if (dest.getSoldeActuel().compareTo(originale.getMontant()) < 0) {
        throw new ValidationException("Solde insuffisant pour annuler cette entrée");
      }
      dest.setSoldeActuel(dest.getSoldeActuel().subtract(originale.getMontant()));
      inverse.setType(TypeTransaction.SORTIE);
      inverse.setCompteSource(dest);
    } else if (originale.getType() == TypeTransaction.SORTIE) {
      // Annuler une sortie = entrée sur le compte source
      Compte src = originale.getCompteSource();
      if (src == null) throw new ValidationException("Transaction invalide (compte source manquant)");
      src.setSoldeActuel(src.getSoldeActuel().add(originale.getMontant()));
      inverse.setType(TypeTransaction.ENTREE);
      inverse.setCompteDestination(src);
    } else if (originale.getType() == TypeTransaction.TRANSFERT) {
      Compte src = originale.getCompteSource();
      Compte dest = originale.getCompteDestination();
      if (src == null || dest == null) throw new ValidationException("Transaction invalide (comptes manquants)");
      if (dest.getSoldeActuel().compareTo(originale.getMontant()) < 0) {
        throw new ValidationException("Solde insuffisant sur le compte destination pour annuler ce transfert");
      }
      // inverse: transfert dans l'autre sens
      dest.setSoldeActuel(dest.getSoldeActuel().subtract(originale.getMontant()));
      src.setSoldeActuel(src.getSoldeActuel().add(originale.getMontant()));
      inverse.setType(TypeTransaction.TRANSFERT);
      inverse.setCompteSource(dest);
      inverse.setCompteDestination(src);
    } else {
      throw new ValidationException("Type transaction non supporté");
    }

    originale.setStatut(StatutTransactionTresorerie.ANNULEE);
    originale.setDateAnnulation(Instant.now());
    originale.setMotifAnnulation(normaliserDescription(motif));

    TransactionTresorerie sauvegardeInverse = transactionRepository.save(inverse);
    journalAuditService.enregistrer("TRESORERIE_ANNULATION", "TransactionTresorerie", originale.getId(),
        "Annulation tx=" + originale.getId() + " inverseId=" + sauvegardeInverse.getId());
    return versResponse(sauvegardeInverse);
  }

  @Transactional(readOnly = true)
  public Page<TransactionTresorerieResponse> listerTransactions(
      TypeTransaction type,
      String recherche,
      LocalDate debut,
      LocalDate fin,
      Pageable pageable) {
    Instant debutInstant = debut == null ? null : debut.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant finExclus = fin == null ? null : fin.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Pageable tri =
        pageable.getSort().isUnsorted()
            ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateOperation"))
            : pageable;
    var spec = TransactionTresorerieSpecs.pourListeJournal(type, debutInstant, finExclus, recherche);
    return transactionRepository.findAll(spec, tri).map(this::versResponse);
  }

  /**
   * Encaissements / décaissements par mois civil (date d’opération), hors transferts et hors transactions annulées.
   */
  @Transactional(readOnly = true)
  public List<FluxMoisRapportResponse> rapportMensuel(YearMonth debutYm, YearMonth finYm) {
    if (debutYm.isAfter(finYm)) {
      throw new ValidationException("La période debut doit précéder ou égaler fin");
    }
    LocalDate debut = debutYm.atDay(1);
    LocalDate fin = finYm.atEndOfMonth();
    Map<String, FluxMoisRapportResponse> map = new HashMap<>();
    for (Object[] row : transactionRepository.fluxParMois(debut, fin)) {
      String mois = String.valueOf(row[0]);
      BigDecimal enc = new BigDecimal(String.valueOf(row[1]));
      BigDecimal dec = new BigDecimal(String.valueOf(row[2]));
      map.put(mois, new FluxMoisRapportResponse(mois, enc, dec));
    }
    List<FluxMoisRapportResponse> out = new ArrayList<>();
    for (YearMonth ym = debutYm; !ym.isAfter(finYm); ym = ym.plusMonths(1)) {
      String key = ym.toString();
      out.add(map.getOrDefault(key, new FluxMoisRapportResponse(key, ZERO, ZERO)));
    }
    return out;
  }

  private TransactionTresorerieResponse versResponse(TransactionTresorerie tx) {
    String libelle = libelleAffiche(tx);
    String ref = referenceAffiche(tx);
    String compteNom;
    String nomSource;
    String nomDestination;
    if (tx.getType() == TypeTransaction.TRANSFERT) {
      compteNom = null;
      nomSource = tx.getCompteSource() == null ? null : tx.getCompteSource().getNom();
      nomDestination = tx.getCompteDestination() == null ? null : tx.getCompteDestination().getNom();
    } else if (tx.getType() == TypeTransaction.ENTREE) {
      compteNom = tx.getCompteDestination() == null ? null : tx.getCompteDestination().getNom();
      nomSource = null;
      nomDestination = null;
    } else {
      compteNom = tx.getCompteSource() == null ? null : tx.getCompteSource().getNom();
      nomSource = null;
      nomDestination = null;
    }
    return new TransactionTresorerieResponse(
        tx.getId(),
        tx.getDateOperation(),
        tx.getType(),
        libelle,
        tx.getMontant(),
        devise,
        compteNom,
        nomSource,
        nomDestination,
        ref,
        tx.getStatut());
  }

  private static String libelleAffiche(TransactionTresorerie tx) {
    String d = tx.getDescription();
    if (d != null && !d.isBlank()) {
      return d.trim();
    }
    return switch (tx.getTypeReference()) {
      case FACTURE -> "Encaissement client";
      case DEPENSE -> "Dépense";
      case PAIE -> "Paiement salaire";
      default -> "Opération trésorerie";
    };
  }

  private static String referenceAffiche(TransactionTresorerie tx) {
    if (tx.getIdReference() != null) {
      return tx.getTypeReference().name() + ":" + tx.getIdReference();
    }
    if (tx.getTypeReference() != TypeReference.MANUEL) {
      return tx.getTypeReference().name();
    }
    return null;
  }

  private String normaliserDescription(String description) {
    if (description == null) {
      return null;
    }
    String d = description.trim();
    return d.isEmpty() ? null : d;
  }
}
