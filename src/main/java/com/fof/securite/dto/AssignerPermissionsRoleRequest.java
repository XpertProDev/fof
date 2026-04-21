package com.fof.securite.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AssignerPermissionsRoleRequest(
    /**
     * Liste des codes permissions à appliquer au rôle.
     * Peut être vide pour retirer toutes les permissions.
     */
    @NotNull Set<@Size(max = 80) String> codesPermissions
) {}

