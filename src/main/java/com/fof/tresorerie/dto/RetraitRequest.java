package com.fof.tresorerie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RetraitRequest(
    @NotNull Long compteSourceId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    String description
) {}

