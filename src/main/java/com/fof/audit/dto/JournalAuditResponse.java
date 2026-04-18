package com.fof.audit.dto;

import java.time.Instant;

public record JournalAuditResponse(
    Long id,
    Long utilisateurId,
    String action,
    String typeEntite,
    Long idEntite,
    Instant horodatage,
    String details
) {}

