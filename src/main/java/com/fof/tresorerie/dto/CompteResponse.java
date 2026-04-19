package com.fof.tresorerie.dto;

import com.fof.tresorerie.entity.TypeCompte;
import java.math.BigDecimal;

public record CompteResponse(
    Long id,
    String nom,
    TypeCompte type,
    BigDecimal solde,
    String devise
) {}

