package com.fof.dashboard.dto;

import java.math.BigDecimal;

public record MoisTotalResponse(
    String mois, // format YYYY-MM
    BigDecimal total
) {}

