package com.fof.facture.dto;

import com.fof.facture.entity.ModeTva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreerFactureRequest(
    @NotNull Long clientId,
    LocalDate dateEmission,
    LocalDate dateEcheance,
    @NotNull ModeTva modeTva,
    @DecimalMin(value = "0.00") BigDecimal tauxTva,
    @NotEmpty List<@Valid CreerLigneFactureRequest> lignes
) {}

