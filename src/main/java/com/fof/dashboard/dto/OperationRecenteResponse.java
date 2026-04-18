package com.fof.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationRecenteResponse(
    Long id,
    String type, // ENCAISSEMENT | DECAISSEMENT | TRANSFERT
    String label,
    String detail,
    BigDecimal montant,
    String devise,
    Instant date
) {}

