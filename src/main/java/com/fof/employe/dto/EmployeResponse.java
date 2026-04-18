package com.fof.employe.dto;

import com.fof.employe.entity.StatutEmploye;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EmployeResponse(
    Long id,
    String nom,
    String prenom,
    String telephone,
    String fonction,
    BigDecimal salaireBase,
    String typeContrat,
    String photoUrl,
    LocalDate dateEmbauche,
    StatutEmploye statut,
    Instant dateCreation
) {}

