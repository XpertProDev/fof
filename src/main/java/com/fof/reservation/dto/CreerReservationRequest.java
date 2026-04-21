package com.fof.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreerReservationRequest(
    @NotBlank String referenceReservation,
    @NotNull LocalDate dateReservation,
    @NotBlank String agence,
    @NotNull @Valid PassagerDto passager,
    @NotNull @Valid BilletDto billet,
    @NotEmpty @Valid List<VolDto> vols
) {}
