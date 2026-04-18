package com.fof.paie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PayerPaieRequest(
    @NotNull Long compteSourceId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    @Size(max = 255) String commentaire
) {}

