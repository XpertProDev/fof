package com.fof.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VolDto(
    @NotBlank String numeroVol,
    @NotBlank String compagnie,
    @NotBlank String classeVoyage,
    String avion,
    String franchiseBagage,
    @NotNull @Valid AeroportInfoDto depart,
    @NotNull @Valid AeroportInfoDto arrivee,
    @Valid List<EscaleDto> escales
) {}
