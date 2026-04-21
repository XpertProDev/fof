package com.fof.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public record PassagerDto(
    @NotBlank String prenom,
    @NotBlank String nom
) {}
