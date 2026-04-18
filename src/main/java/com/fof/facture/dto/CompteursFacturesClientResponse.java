package com.fof.facture.dto;

public record CompteursFacturesClientResponse(
    long nombrePayees,
    long nombreImpayees,
    long nombreTotal
) {}
