package com.fof.securite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreerRoleRequest(
    @NotBlank @Size(max = 60) String nom
) {}

