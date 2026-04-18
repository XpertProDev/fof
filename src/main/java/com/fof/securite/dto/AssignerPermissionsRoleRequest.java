package com.fof.securite.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AssignerPermissionsRoleRequest(
    @NotEmpty Set<@Size(max = 80) String> codesPermissions
) {}

