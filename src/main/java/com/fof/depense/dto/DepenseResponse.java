package com.fof.depense.dto;

import com.fof.depense.entity.StatutDepense;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DepenseResponse(
    Long id,
    String titre,
    BigDecimal montant,
    String categorie,
    LocalDate dateDepense,
    String description,
    StatutDepense statut,
    Long comptePaiementId,
    Instant datePaiement,
    Instant dateCreation
) {}

