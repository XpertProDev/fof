package com.fof.facture.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaiementFactureResponse(
    Long id,
    BigDecimal montant,
    Instant datePaiement,
    Long compteDestinationId,
    String reference,
    String commentaire
) {}

