package com.fof.securite.dto;

import com.fof.securite.entity.StatutUtilisateur;
import java.time.Instant;
import java.util.Set;

public record UtilisateurResponse(
    Long id,
    String nom,
    String prenom,
    String telephone,
    String email,
    StatutUtilisateur statut,
    Instant dateCreation,
    Set<String> roles
) {}

