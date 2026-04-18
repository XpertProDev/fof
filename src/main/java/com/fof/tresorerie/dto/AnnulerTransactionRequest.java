package com.fof.tresorerie.dto;

import jakarta.validation.constraints.Size;

public record AnnulerTransactionRequest(
    @Size(max = 255) String motif
) {}

