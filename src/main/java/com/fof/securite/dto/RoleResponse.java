package com.fof.securite.dto;

import java.util.Set;

public record RoleResponse(
    Long id,
    String nom,
    Set<String> permissions
) {}

