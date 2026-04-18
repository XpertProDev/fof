package com.fof.securite.dto;

import com.fof.securite.entity.StatutUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreerUtilisateurRequest(
    @NotBlank @Size(max = 80) String nom,
    @NotBlank @Size(max = 80) String prenom,
    @Size(max = 30) String telephone,
    @Email @NotBlank @Size(max = 160) String email,
    @NotBlank @Size(min = 6, max = 100) String motDePasse,
    StatutUtilisateur statut
) {}

