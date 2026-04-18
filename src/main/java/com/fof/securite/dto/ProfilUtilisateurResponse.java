package com.fof.securite.dto;

import com.fof.securite.entity.StatutUtilisateur;
import java.time.Instant;
import java.util.Set;

public record ProfilUtilisateurResponse(
    Long id,
    String nom,
    String prenom,
    String telephone,
    String email,
    String photoUrl,
    StatutUtilisateur statut,
    Instant dateCreation,
    Set<String> roles,
    Set<String> permissions
) {}

