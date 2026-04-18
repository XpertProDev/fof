package com.fof.dashboard.dto;

import java.math.BigDecimal;

public record SoldeCompteResponse(
    Long compteId,
    String compteNom,
    BigDecimal solde,
    String devise
) {}

