package com.fof.securite.service;

import com.fof.securite.dto.ModifierProfilRequest;
import com.fof.securite.dto.ProfilUtilisateurResponse;
import com.fof.securite.entity.Permission;
import com.fof.securite.entity.Role;
import com.fof.securite.entity.Utilisateur;
import com.fof.securite.repository.UtilisateurRepository;
import com.fof.fichier.service.StockageFichierService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ProfilService {

  private final UtilisateurRepository utilisateurRepository;
  private final StockageFichierService stockageFichierService;

  @Transactional(readOnly = true)
  public ProfilUtilisateurResponse moi() {
    Utilisateur u = chargerUtilisateurCourant();
    return versResponse(u);
  }

  @Transactional
  public ProfilUtilisateurResponse modifierMoi(ModifierProfilRequest request, MultipartFile photo) {
    Utilisateur u = chargerUtilisateurCourant();

    String email = request.email().trim();
    if (!u.getEmail().equalsIgnoreCase(email) && utilisateurRepository.existsByEmailIgnoreCase(email)) {
      throw new ValidationException("Email déjà utilisé");
    }

    u.setNom(request.nom().trim());
    u.setPrenom(request.prenom().trim());
    u.setTelephone(nettoyer(request.telephone()));
    u.setEmail(email);

    if (photo != null && !photo.isEmpty()) {
      String ancienne = u.getPhotoUrl();
      String url = stockageFichierService.enregistrerImage(photo, "userUpload");
      u.setPhotoUrl(url);
      stockageFichierService.supprimerSiPossible(ancienne);
    }

    return versResponse(u);
  }

  private Utilisateur chargerUtilisateurCourant() {
    Long id = utilisateurCourantId();
    if (id == null) throw new ValidationException("Non authentifié");
    return utilisateurRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + id));
  }

  private Long utilisateurCourantId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) return null;
    Object p = auth.getPrincipal();
    if (p instanceof Long l) return l;
    try {
      return Long.parseLong(String.valueOf(p));
    } catch (Exception e) {
      return null;
    }
  }

  private ProfilUtilisateurResponse versResponse(Utilisateur u) {
    Set<String> roles = u.getRoles().stream().map(Role::getNom).collect(Collectors.toSet());
    Set<String> permissions = u.getRoles().stream()
        .flatMap(r -> r.getPermissions().stream())
        .map(Permission::getCode)
        .collect(Collectors.toSet());
    return new ProfilUtilisateurResponse(
        u.getId(),
        u.getNom(),
        u.getPrenom(),
        u.getTelephone(),
        u.getEmail(),
        u.getPhotoUrl(),
        u.getStatut(),
        u.getDateCreation(),
        roles,
        permissions
    );
  }

  private String nettoyer(String v) {
    if (v == null) return null;
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }
}

