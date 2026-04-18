package com.fof.client.dto;

import com.fof.client.entity.StatutClient;
import java.time.Instant;

public record ClientResponse(
    Long id,
    String nomComplet,
    String telephone,
    String email,
    String adresse,
    String pays,
    String photoUrl,
    Instant dateCreation,
    StatutClient statut
) {}

