package com.fof.securite.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AssignerRolesUtilisateurRequest(
    @NotEmpty Set<@Size(max = 60) String> nomsRoles
) {}

