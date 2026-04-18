package com.fof.paie.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreerPaieRequest(
    @NotNull Long employeId,
    @NotBlank @Size(max = 7) String mois, // YYYY-MM
    @NotNull @DecimalMin(value = "0.00") BigDecimal primes,
    @NotNull @DecimalMin(value = "0.00") BigDecimal deductions
) {}

