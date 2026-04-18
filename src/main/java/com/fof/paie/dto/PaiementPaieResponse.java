package com.fof.paie.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaiementPaieResponse(
    Long id,
    BigDecimal montant,
    Instant datePaiement,
    Long compteSourceId,
    String commentaire
) {}

