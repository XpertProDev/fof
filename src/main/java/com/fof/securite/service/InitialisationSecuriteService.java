package com.fof.securite.service;

import com.fof.securite.entity.Permission;
import com.fof.securite.entity.Role;
import com.fof.securite.entity.StatutUtilisateur;
import com.fof.securite.entity.Utilisateur;
import com.fof.securite.repository.PermissionRepository;
import com.fof.securite.repository.RoleRepository;
import com.fof.securite.repository.UtilisateurRepository;
import jakarta.validation.ValidationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class InitialisationSecuriteService {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.email}")
  private String adminEmail;

  @Value("${app.admin.mot-de-passe}")
  private String adminMotDePasse;

  @Value("${app.admin.nom:Admin}")
  private String adminNom;

  @Value("${app.admin.prenom:Principal}")
  private String adminPrenom;

  private static final List<String> PERMISSIONS = List.of(
      "FACTURE_CREATE",
      "FACTURE_READ",
      "FACTURE_UPDATE",
      "FACTURE_DELETE",
      "CLIENT_MANAGE",
      "EMPLOYEE_MANAGE",
      "PAYROLL_MANAGE",
      "EXPENSE_MANAGE",
      "ACCOUNTING_VIEW",
      "TREASURY_MANAGE",
      "SETTINGS_MANAGE",
      "RESERVATION_MANAGE"
  );

  @Transactional
  public void initialiserSiBesoin() {
    // 1) Permissions
    for (String code : PERMISSIONS) {
      permissionRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
        Permission p = new Permission();
        p.setCode(code);
        p.setLibelle(code);
        return permissionRepository.save(p);
      });
    }

    // 2) Rôle ADMIN avec toutes permissions
    Role adminRole = roleRepository.findByNomIgnoreCase("ADMIN").orElseGet(() -> {
      Role r = new Role();
      r.setNom("ADMIN");
      return roleRepository.save(r);
    });
    Set<Permission> toutes = new LinkedHashSet<>(permissionRepository.findAll());
    adminRole.setPermissions(toutes);

    // 3) Admin utilisateur
    String email = adminEmail == null ? null : adminEmail.trim();
    if (email == null || email.isEmpty()) {
      throw new ValidationException("app.admin.email est obligatoire");
    }

    Utilisateur admin = utilisateurRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
      Utilisateur u = new Utilisateur();
      u.setEmail(email);
      u.setNom(adminNom == null ? "Admin" : adminNom.trim());
      u.setPrenom(adminPrenom == null ? "Principal" : adminPrenom.trim());
      u.setMotDePasseHash(passwordEncoder.encode(adminMotDePasse));
      u.setStatut(StatutUtilisateur.ACTIF);
      return utilisateurRepository.save(u);
    });
    admin.getRoles().add(adminRole);
  }
}

