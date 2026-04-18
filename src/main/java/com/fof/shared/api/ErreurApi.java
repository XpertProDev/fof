package com.fof.shared.api;

import java.time.Instant;
import java.util.List;

public record ErreurApi(
    Instant horodatage,
    int statut,
    String message,
    List<String> details
) {}

