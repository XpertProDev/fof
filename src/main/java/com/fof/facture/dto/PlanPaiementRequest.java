package com.fof.facture.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PlanPaiementRequest(
    @NotEmpty List<@Valid CreerEcheancePaiementRequest> echeances
) {}

