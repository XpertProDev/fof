package com.fof.facture.dto;

import com.fof.facture.entity.StatutEcheancePaiement;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EcheancePaiementResponse(
    Long id,
    Long factureId,
    BigDecimal montantProgramme,
    BigDecimal montantPaye,
    LocalDate datePrevue,
    StatutEcheancePaiement statut,
    String commentaire,
    Instant datePaiement
) {}

