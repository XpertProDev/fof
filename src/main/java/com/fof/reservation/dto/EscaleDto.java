package com.fof.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record EscaleDto(
    @NotBlank String villeDepart,
    @NotBlank String villeArrivee
) {}
