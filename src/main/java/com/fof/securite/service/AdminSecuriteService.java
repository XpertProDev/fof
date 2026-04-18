package com.fof.securite.service;

import com.fof.securite.dto.AssignerPermissionsRoleRequest;
import com.fof.securite.dto.AssignerRolesUtilisateurRequest;
import com.fof.securite.dto.ChangerMotDePasseRequest;
import com.fof.securite.dto.CreerRoleRequest;
import com.fof.securite.dto.CreerUtilisateurRequest;
import com.fof.securite.dto.ModifierUtilisateurRequest;
import com.fof.securite.dto.PermissionResponse;
import com.fof.securite.dto.RoleResponse;
import com.fof.securite.dto.UtilisateurResponse;
import com.fof.securite.entity.Permission;
import com.fof.securite.entity.Role;
import com.fof.securite.entity.StatutUtilisateur;
import com.fof.securite.entity.Utilisateur;
import com.fof.securite.repository.PermissionRepository;
import com.fof.securite.repository.RoleRepository;
import com.fof.securite.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminSecuriteService {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public Page<PermissionResponse> listerPermissions(String recherche, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return permissionRepository.rechercher(q, pageable).map(this::versPermissionResponse);
  }

  @Transactional
  public RoleResponse creerRole(CreerRoleRequest request) {
    Objects.requireNonNull(request, "request");
    String nom = request.nom().trim();
    if (roleRepository.findByNomIgnoreCase(nom).isPresent()) {
      throw new ValidationException("Rôle existe déjà");
    }
    Role r = new Role();
    r.setNom(nom);
    Role sauvegarde = roleRepository.save(r);
    return versRoleResponse(sauvegarde);
  }

  @Transactional(readOnly = true)
  public Page<RoleResponse> listerRoles(String recherche, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return roleRepository.rechercher(q, pageable).map(this::versRoleResponse);
  }

  @Transactional
  public RoleResponse assignerPermissionsRole(Long roleId, AssignerPermissionsRoleRequest request) {
    Objects.requireNonNull(request, "request");
    Role role = roleRepository.findById(roleId).orElseThrow(() -> new EntityNotFoundException("Rôle introuvable: " + roleId));
    Set<Permission> permissions = new HashSet<>();
    for (String code : request.codesPermissions()) {
      Permission p = permissionRepository.findByCodeIgnoreCase(code.trim())
          .orElseThrow(() -> new ValidationException("Permission introuvable: " + code));
      permissions.add(p);
    }
    role.setPermissions(permissions);
    return versRoleResponse(role);
  }

  @Transactional
  public void supprimerRole(Long roleId) {
    if (!roleRepository.existsById(roleId)) {
      throw new EntityNotFoundException("Rôle introuvable: " + roleId);
    }
    roleRepository.deleteById(roleId);
  }

  @Transactional
  public UtilisateurResponse creerUtilisateur(CreerUtilisateurRequest request) {
    Objects.requireNonNull(request, "request");
    String email = request.email().trim();
    if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
      throw new ValidationException("Email déjà utilisé");
    }
    Utilisateur u = new Utilisateur();
    u.setNom(request.nom().trim());
    u.setPrenom(request.prenom().trim());
    u.setTelephone(nettoyer(request.telephone()));
    u.setEmail(email);
    u.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));
    u.setStatut(request.statut() == null ? StatutUtilisateur.ACTIF : request.statut());
    Utilisateur sauvegarde = utilisateurRepository.save(u);
    return versUtilisateurResponse(sauvegarde);
  }

  @Transactional
  public UtilisateurResponse modifierUtilisateur(Long id, ModifierUtilisateurRequest request) {
    Objects.requireNonNull(request, "request");
    Utilisateur u = utilisateurRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + id));
    String email = request.email().trim();
    if (!u.getEmail().equalsIgnoreCase(email) && utilisateurRepository.existsByEmailIgnoreCase(email)) {
      throw new ValidationException("Email déjà utilisé");
    }
    u.setNom(request.nom().trim());
    u.setPrenom(request.prenom().trim());
    u.setTelephone(nettoyer(request.telephone()));
    u.setEmail(email);
    u.setStatut(request.statut() == null ? u.getStatut() : request.statut());
    return versUtilisateurResponse(u);
  }

  @Transactional(readOnly = true)
  public Page<UtilisateurResponse> listerUtilisateurs(String recherche, Pageable pageable) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    return utilisateurRepository.rechercher(q, pageable).map(this::versUtilisateurResponse);
  }

  @Transactional
  public UtilisateurResponse assignerRolesUtilisateur(Long utilisateurId, AssignerRolesUtilisateurRequest request) {
    Objects.requireNonNull(request, "request");
    Utilisateur u = utilisateurRepository.findById(utilisateurId)
        .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + utilisateurId));
    Set<Role> roles = new HashSet<>();
    for (String nomRole : request.nomsRoles()) {
      Role role = roleRepository.findByNomIgnoreCase(nomRole.trim())
          .orElseThrow(() -> new ValidationException("Rôle introuvable: " + nomRole));
      roles.add(role);
    }
    u.setRoles(roles);
    return versUtilisateurResponse(u);
  }

  @Transactional
  public void changerMotDePasse(Long utilisateurId, ChangerMotDePasseRequest request) {
    Objects.requireNonNull(request, "request");
    Utilisateur u = utilisateurRepository.findById(utilisateurId)
        .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + utilisateurId));
    u.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
  }

  @Transactional
  public void supprimerUtilisateur(Long utilisateurId) {
    if (!utilisateurRepository.existsById(utilisateurId)) {
      throw new EntityNotFoundException("Utilisateur introuvable: " + utilisateurId);
    }
    utilisateurRepository.deleteById(utilisateurId);
  }

  private PermissionResponse versPermissionResponse(Permission p) {
    return new PermissionResponse(p.getId(), p.getCode(), p.getLibelle());
  }

  private RoleResponse versRoleResponse(Role r) {
    Set<String> perms = r.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
    return new RoleResponse(r.getId(), r.getNom(), perms);
  }

  private UtilisateurResponse versUtilisateurResponse(Utilisateur u) {
    Set<String> roles = u.getRoles().stream().map(Role::getNom).collect(Collectors.toSet());
    return new UtilisateurResponse(
        u.getId(),
        u.getNom(),
        u.getPrenom(),
        u.getTelephone(),
        u.getEmail(),
        u.getStatut(),
        u.getDateCreation(),
        roles
    );
  }

  private String nettoyer(String v) {
    if (v == null) return null;
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }
}

