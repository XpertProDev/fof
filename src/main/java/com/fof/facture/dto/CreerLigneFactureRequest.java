package com.fof.facture.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreerLigneFactureRequest(
    @NotBlank @Size(max = 255) String description,
    @NotNull @DecimalMin(value = "0.01") BigDecimal quantite,
    @NotNull @DecimalMin(value = "0.01") BigDecimal prixUnitaire
) {}

