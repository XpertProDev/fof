package com.fof.facture.dto;

import java.math.BigDecimal;

public record LigneFactureResponse(
    Long id,
    String description,
    BigDecimal quantite,
    BigDecimal prixUnitaire,
    BigDecimal totalLigne
) {}

