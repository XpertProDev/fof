package com.fof.facture.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record FacturesClientListeResponse(
    CompteursFacturesClientResponse compteurs,
    List<FactureResponse> factures,
    long totalElements,
    int totalPages,
    int page,
    int size
) {
  public static FacturesClientListeResponse of(CompteursFacturesClientResponse compteurs, Page<FactureResponse> page) {
    return new FacturesClientListeResponse(
        compteurs,
        page.getContent(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.getNumber(),
        page.getSize()
    );
  }
}
