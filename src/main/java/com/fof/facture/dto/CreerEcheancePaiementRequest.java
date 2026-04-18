package com.fof.facture.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreerEcheancePaiementRequest(
    @NotNull @DecimalMin(value = "0.01") BigDecimal montantProgramme,
    @NotNull LocalDate datePrevue,
    @Size(max = 255) String commentaire
) {}

