package com.fof.entreprise.dto;


public record EntrepriseInfoResponse(
    Long id,
    String nom,
    String telephone,
    String email,
    String adresse,
    String ville,
    String pays,
    String ninea,
    String rccm,
    String nomBanque,
    String numeroCompteBanque,
    String logoUrl
) {}

