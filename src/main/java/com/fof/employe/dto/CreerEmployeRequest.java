package com.fof.employe.dto;

import com.fof.employe.entity.StatutEmploye;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreerEmployeRequest(
    @NotBlank @Size(max = 80) String nom,
    @NotBlank @Size(max = 80) String prenom,
    @Size(max = 30) String telephone,
    @Size(max = 80) String fonction,
    @NotNull @DecimalMin(value = "0.01") BigDecimal salaireBase,
    @Size(max = 50) String typeContrat,
    LocalDate dateEmbauche,
    StatutEmploye statut
) {}

