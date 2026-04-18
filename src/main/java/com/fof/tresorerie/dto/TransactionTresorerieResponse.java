package com.fof.tresorerie.dto;

import com.fof.tresorerie.entity.TypeReference;
import com.fof.tresorerie.entity.TypeTransaction;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionTresorerieResponse(
    Long id,
    TypeTransaction type,
    BigDecimal montant,
    Instant dateOperation,
    Long compteSourceId,
    Long compteDestinationId,
    TypeReference typeReference,
    Long idReference,
    String description
) {}

