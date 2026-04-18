package com.fof.securite.controller;

import com.fof.securite.dto.AssignerPermissionsRoleRequest;
import com.fof.securite.dto.AssignerRolesUtilisateurRequest;
import com.fof.securite.dto.ChangerMotDePasseRequest;
import com.fof.securite.dto.CreerRoleRequest;
import com.fof.securite.dto.CreerUtilisateurRequest;
import com.fof.securite.dto.ModifierUtilisateurRequest;
import com.fof.securite.dto.PermissionResponse;
import com.fof.securite.dto.RoleResponse;
import com.fof.securite.dto.UtilisateurResponse;
import com.fof.securite.service.AdminSecuriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/securite")
@PreAuthorize("hasAuthority('PERM_SETTINGS_MANAGE')")
public class AdminSecuriteController {

  private final AdminSecuriteService adminSecuriteService;

  // Permissions
  @GetMapping("/permissions")
  public Page<PermissionResponse> permissions(String recherche, @PageableDefault(size = 50) Pageable pageable) {
    return adminSecuriteService.listerPermissions(recherche, pageable);
  }

  // Rôles
  @PostMapping("/roles")
  public RoleResponse creerRole(@Valid @RequestBody CreerRoleRequest request) {
    return adminSecuriteService.creerRole(request);
  }

  @GetMapping("/roles")
  public Page<RoleResponse> listerRoles(String recherche, @PageableDefault(size = 50) Pageable pageable) {
    return adminSecuriteService.listerRoles(recherche, pageable);
  }

  @PutMapping("/roles/{roleId}/permissions")
  public RoleResponse assignerPermissions(@PathVariable Long roleId, @Valid @RequestBody AssignerPermissionsRoleRequest request) {
    return adminSecuriteService.assignerPermissionsRole(roleId, request);
  }

  @DeleteMapping("/roles/{roleId}")
  public void supprimerRole(@PathVariable Long roleId) {
    adminSecuriteService.supprimerRole(roleId);
  }

  // Utilisateurs
  @PostMapping("/utilisateurs")
  public UtilisateurResponse creerUtilisateur(@Valid @RequestBody CreerUtilisateurRequest request) {
    return adminSecuriteService.creerUtilisateur(request);
  }

  @PutMapping("/utilisateurs/{id}")
  public UtilisateurResponse modifierUtilisateur(@PathVariable Long id, @Valid @RequestBody ModifierUtilisateurRequest request) {
    return adminSecuriteService.modifierUtilisateur(id, request);
  }

  @GetMapping("/utilisateurs")
  public Page<UtilisateurResponse> listerUtilisateurs(String recherche, @PageableDefault(size = 50) Pageable pageable) {
    return adminSecuriteService.listerUtilisateurs(recherche, pageable);
  }

  @PutMapping("/utilisateurs/{id}/roles")
  public UtilisateurResponse assignerRoles(@PathVariable Long id, @Valid @RequestBody AssignerRolesUtilisateurRequest request) {
    return adminSecuriteService.assignerRolesUtilisateur(id, request);
  }

  @PutMapping("/utilisateurs/{id}/mot-de-passe")
  public void changerMotDePasse(@PathVariable Long id, @Valid @RequestBody ChangerMotDePasseRequest request) {
    adminSecuriteService.changerMotDePasse(id, request);
  }

  @DeleteMapping("/utilisateurs/{id}")
  public void supprimerUtilisateur(@PathVariable Long id) {
    adminSecuriteService.supprimerUtilisateur(id);
  }
}

