package com.fof.depense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreerDepenseRequest(
    @NotBlank @Size(max = 160) String titre,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    @Size(max = 80) String categorie,
    LocalDate dateDepense,
    @Size(max = 255) String description
) {}

