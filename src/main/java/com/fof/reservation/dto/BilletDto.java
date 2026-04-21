package com.fof.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record BilletDto(
    @NotBlank String numeroBillet,
    @NotBlank String type,
    @NotBlank String compagnieEmission
) {}
