package com.fof.facture.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PaiementFactureRequest(
    @NotNull Long compteDestinationId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal montant,
    @Size(max = 80) String reference,
    @Size(max = 255) String commentaire
) {}

