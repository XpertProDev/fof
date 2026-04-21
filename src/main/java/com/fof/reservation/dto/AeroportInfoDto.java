package com.fof.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AeroportInfoDto(
    @NotBlank String ville,
    @NotBlank String aeroport,
    String terminal,
    @NotNull LocalDateTime dateHeure
) {}
