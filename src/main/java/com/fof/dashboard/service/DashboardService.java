package com.fof.dashboard.service;

import com.fof.dashboard.dto.DashboardResponse;
import com.fof.dashboard.dto.FluxParMoisResponse;
import com.fof.dashboard.dto.MoisTotalResponse;
import com.fof.dashboard.dto.OperationRecenteResponse;
import com.fof.dashboard.dto.SoldeCompteResponse;
import com.fof.depense.repository.DepenseRepository;
import com.fof.facture.entity.StatutFacture;
import com.fof.facture.repository.FactureRepository;
import com.fof.paie.repository.PaieRepository;
import com.fof.tresorerie.entity.TransactionTresorerie;
import com.fof.tresorerie.repository.TransactionTresorerieRepository;
import com.fof.tresorerie.repository.CompteRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DashboardService {

  private final FactureRepository factureRepository;
  private final DepenseRepository depenseRepository;
  private final PaieRepository paieRepository;
  private final CompteRepository compteRepository;
  private final TransactionTresorerieRepository transactionTresorerieRepository;

  @Value("${app.devise:XOF}")
  private String devise;

  @Transactional(readOnly = true)
  public DashboardResponse chargerDashboard(Integer annee, Integer tailleOperationsRecentes) {
    BigDecimal ca = factureRepository.sommeChiffreAffairesTotal();
    BigDecimal dettes = factureRepository.sommeDettesClients();
    long facturesEnAttente = factureRepository.compterFacturesEnAttente();

    YearMonth ym = YearMonth.now();
    LocalDate debutMois = ym.atDay(1);
    LocalDate finMois = ym.atEndOfMonth();
    BigDecimal depensesMois = depenseRepository.sommeDepensesEntre(debutMois, finMois);

    BigDecimal salairesAPayer = paieRepository.sommeSalairesApprouvesAPayer();
    BigDecimal soldeGlobal = compteRepository.sommeSoldeGlobal();

    List<SoldeCompteResponse> soldesParCompte = compteRepository.findAll().stream()
        .map(c -> new SoldeCompteResponse(c.getId(), c.getNom(), c.getSoldeActuel(), devise))
        .toList();

    // Période pour séries (12 mois)
    List<YearMonth> moisSeries = construire12Mois(annee);
    LocalDate debutSerie = moisSeries.getFirst().atDay(1);
    LocalDate finSerie = moisSeries.getLast().atEndOfMonth();

    List<MoisTotalResponse> chiffreAffairesParMois = remplirSerieMoisTotal(
        moisSeries,
        factureRepository.chiffreAffairesParMois(debutSerie, finSerie)
    );

    List<MoisTotalResponse> depensesParMois = remplirSerieMoisTotal(
        moisSeries,
        depenseRepository.depensesParMois(debutSerie, finSerie)
    );

    List<FluxParMoisResponse> fluxParMois = remplirSerieFlux(
        moisSeries,
        transactionTresorerieRepository.fluxParMois(debutSerie, finSerie)
    );

    // Opérations récentes (trésorerie + ventes)
    int taille = (tailleOperationsRecentes == null || tailleOperationsRecentes < 5) ? 10 : Math.min(tailleOperationsRecentes, 15);
    List<TransactionTresorerie> recentes = transactionTresorerieRepository
        .findAllByOrderByDateOperationDesc(PageRequest.of(0, taille, Sort.by(Sort.Direction.DESC, "dateOperation")))
        .getContent();
    List<OperationRecenteResponse> operationsRecentes = recentes.stream().map(this::versOperationRecente).toList();

    // Factures par statut (snapshot)
    Map<String, Long> facturesParStatut = initialiserFacturesParStatut();
    for (Object[] row : factureRepository.compterParStatut()) {
      String statut = String.valueOf(row[0]);
      Long total = ((Number) row[1]).longValue();
      facturesParStatut.put(statut, total);
    }

    return new DashboardResponse(
        devise,
        ca,
        dettes,
        facturesEnAttente,
        depensesMois,
        salairesAPayer,
        soldeGlobal,
        soldesParCompte,
        chiffreAffairesParMois,
        depensesParMois,
        fluxParMois,
        operationsRecentes,
        facturesParStatut
    );
  }

  private List<YearMonth> construire12Mois(Integer annee) {
    if (annee != null) {
      List<YearMonth> mois = new ArrayList<>(12);
      for (int m = 1; m <= 12; m++) {
        mois.add(YearMonth.of(annee, m));
      }
      return mois;
    }
    YearMonth fin = YearMonth.now();
    List<YearMonth> mois = new ArrayList<>(12);
    for (int i = 11; i >= 0; i--) {
      mois.add(fin.minusMonths(i));
    }
    return mois;
  }

  private List<MoisTotalResponse> remplirSerieMoisTotal(List<YearMonth> moisSeries, List<Object[]> bruts) {
    Map<String, BigDecimal> map = new java.util.HashMap<>();
    for (Object[] row : bruts) {
      String mois = String.valueOf(row[0]);
      BigDecimal total = new BigDecimal(String.valueOf(row[1]));
      map.put(mois, total);
    }
    return moisSeries.stream()
        .map(m -> new MoisTotalResponse(m.toString(), map.getOrDefault(m.toString(), BigDecimal.ZERO)))
        .toList();
  }

  private List<FluxParMoisResponse> remplirSerieFlux(List<YearMonth> moisSeries, List<Object[]> bruts) {
    Map<String, FluxParMoisResponse> map = new java.util.HashMap<>();
    for (Object[] row : bruts) {
      String mois = String.valueOf(row[0]);
      BigDecimal enc = new BigDecimal(String.valueOf(row[1]));
      BigDecimal dec = new BigDecimal(String.valueOf(row[2]));
      map.put(mois, new FluxParMoisResponse(mois, enc, dec));
    }
    return moisSeries.stream()
        .map(m -> map.getOrDefault(m.toString(), new FluxParMoisResponse(m.toString(), BigDecimal.ZERO, BigDecimal.ZERO)))
        .toList();
  }

  private OperationRecenteResponse versOperationRecente(TransactionTresorerie t) {
    String type = switch (t.getType()) {
      case ENTREE -> "ENCAISSEMENT";
      case SORTIE -> "DECAISSEMENT";
      case TRANSFERT -> "TRANSFERT";
    };

    String label = switch (t.getTypeReference()) {
      case FACTURE -> "Encaissement client";
      case DEPENSE -> "Dépense";
      case PAIE -> "Paiement salaire";
      default -> "Opération trésorerie";
    };

    String detail = t.getDescription();
    if (detail == null || detail.isBlank()) {
      detail = t.getTypeReference() + (t.getIdReference() == null ? "" : (" #" + t.getIdReference()));
    }

    return new OperationRecenteResponse(
        t.getId(),
        type,
        label,
        detail,
        t.getMontant(),
        devise,
        t.getDateOperation()
    );
  }

  private Map<String, Long> initialiserFacturesParStatut() {
    Map<String, Long> map = new java.util.HashMap<>();
    for (StatutFacture s : StatutFacture.values()) {
      if (s == StatutFacture.ANNULEE) continue;
      map.put(s.name(), 0L);
    }
    return map;
  }
}

