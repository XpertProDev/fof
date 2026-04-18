package com.fof.tresorerie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepotRequest(
    @NotNull Long compteDestinationId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    String description
) {}

