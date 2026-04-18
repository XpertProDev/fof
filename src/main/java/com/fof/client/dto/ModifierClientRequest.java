package com.fof.client.dto;

import com.fof.client.entity.StatutClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModifierClientRequest(
    @NotBlank @Size(max = 160) String nomComplet,
    @Size(max = 30) String telephone,
    @Email @Size(max = 160) String email,
    @Size(max = 255) String adresse,
    @Size(max = 80) String pays,
    StatutClient statut
) {}

