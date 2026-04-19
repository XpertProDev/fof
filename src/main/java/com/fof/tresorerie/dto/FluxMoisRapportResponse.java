package com.fof.tresorerie.dto;

import java.math.BigDecimal;

/** Agrégat mensuel pour la page Comptabilité / reporting (hors transferts dans les totaux). */
public record FluxMoisRapportResponse(String mois, BigDecimal encaissements, BigDecimal decaissements) {}
