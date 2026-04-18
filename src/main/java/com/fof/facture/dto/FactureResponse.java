package com.fof.facture.dto;

import com.fof.facture.entity.ModeTva;
import com.fof.facture.entity.StatutFacture;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FactureResponse(
    Long id,
    String numero,
    Long clientId,
    String nomClient,
    LocalDate dateEmission,
    LocalDate dateEcheance,
    StatutFacture statut,
    ModeTva modeTva,
    BigDecimal tauxTva,
    BigDecimal totalHt,
    BigDecimal montantTva,
    BigDecimal totalTtc,
    BigDecimal montantPaye,
    BigDecimal montantRestant,
    List<LigneFactureResponse> lignes,
    List<PaiementFactureResponse> paiements
) {}

