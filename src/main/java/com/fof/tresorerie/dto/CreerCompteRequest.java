package com.fof.tresorerie.dto;

import com.fof.tresorerie.entity.TypeCompte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreerCompteRequest(
    @NotBlank @Size(min = 2, max = 120) String nom,
    @NotNull TypeCompte type
) {}

