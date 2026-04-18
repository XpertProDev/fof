package com.fof.depense.dto;

import jakarta.validation.constraints.NotNull;

public record PayerDepenseRequest(
    @NotNull Long comptePaiementId
) {}

