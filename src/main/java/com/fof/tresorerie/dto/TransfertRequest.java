package com.fof.tresorerie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransfertRequest(
    @NotNull Long compteSourceId,
    @NotNull Long compteDestinationId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    String description
) {}

