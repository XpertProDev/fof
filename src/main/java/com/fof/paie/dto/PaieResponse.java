package com.fof.paie.dto;

import com.fof.paie.entity.StatutPaie;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaieResponse(
    Long id,
    Long employeId,
    String nomEmploye,
    String mois,
    BigDecimal salaireBase,
    BigDecimal primes,
    BigDecimal deductions,
    BigDecimal salaireNet,
    StatutPaie statut,
    Instant dateCreation,
    List<PaiementPaieResponse> paiements
) {}

