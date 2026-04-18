package com.fof.dashboard.dto;

import java.math.BigDecimal;

public record FluxParMoisResponse(
    String mois, // YYYY-MM
    BigDecimal encaissements,
    BigDecimal decaissements
) {}

