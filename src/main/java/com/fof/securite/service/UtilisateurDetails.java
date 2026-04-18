package com.fof.securite.service;

import com.fof.securite.entity.Permission;
import com.fof.securite.entity.Role;
import com.fof.securite.entity.StatutUtilisateur;
import com.fof.securite.entity.Utilisateur;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UtilisateurDetails implements UserDetails {

  private final Utilisateur utilisateur;

  public UtilisateurDetails(Utilisateur utilisateur) {
    this.utilisateur = utilisateur;
  }

  public Utilisateur getUtilisateur() {
    return utilisateur;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<String> permissions = utilisateur.getRoles().stream()
        .flatMap((Role r) -> r.getPermissions().stream())
        .map(Permission::getCode)
        .collect(Collectors.toSet());
    return permissions.stream()
        .map(p -> new SimpleGrantedAuthority("PERM_" + p))
        .toList();
  }

  @Override
  public String getPassword() {
    return utilisateur.getMotDePasseHash();
  }

  @Override
  public String getUsername() {
    return utilisateur.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return utilisateur.getStatut() == StatutUtilisateur.ACTIF;
  }
}

