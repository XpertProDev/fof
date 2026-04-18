package com.fof.entreprise.dto;

import jakarta.validation.constraints.Size;

public record ModifierEntrepriseInfoRequest(
    @Size(max = 160) String nom,
    @Size(max = 120) String telephone,
    @Size(max = 120) String email,
    @Size(max = 255) String adresse,
    @Size(max = 80) String ville,
    @Size(max = 80) String pays,
    @Size(max = 80) String ninea,
    @Size(max = 80) String rccm,
    @Size(max = 120) String nomBanque,
    @Size(max = 120) String numeroCompteBanque
) {}

