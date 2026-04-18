package com.fof.securite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email @NotBlank @Size(max = 160) String email,
    @NotBlank @Size(min = 6, max = 100) String motDePasse
) {}

