package com.fof.securite.service;

import com.fof.securite.dto.RefreshRequest;
import com.fof.securite.dto.TokenResponse;
import com.fof.securite.entity.Permission;
import com.fof.securite.entity.RefreshToken;
import com.fof.securite.entity.Role;
import com.fof.securite.entity.StatutUtilisateur;
import com.fof.securite.entity.Utilisateur;
import com.fof.securite.repository.RefreshTokenRepository;
import com.fof.securite.repository.UtilisateurRepository;
import jakarta.validation.ValidationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthentificationService {

  private final UtilisateurRepository utilisateurRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final HashService hashService;
  private final Duration dureeRefresh;

  public AuthentificationService(
      UtilisateurRepository utilisateurRepository,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder,
      HashService hashService,
      @Value("${app.jwt.refresh-days:30}") long refreshDays
  ) {
    this.utilisateurRepository = utilisateurRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.hashService = hashService;
    this.dureeRefresh = Duration.ofDays(refreshDays);
  }

  @Transactional
  public TokenResponse login(String email, String motDePasse) {
    Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(email.trim())
        .orElseThrow(() -> new ValidationException("Identifiants invalides"));
    if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
      throw new ValidationException("Utilisateur inactif");
    }
    if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasseHash())) {
      throw new ValidationException("Identifiants invalides");
    }

    Set<String> permissions = extrairePermissions(utilisateur);
    String access = jwtService.creerAccessToken(utilisateur, permissions);

    String refreshClair = UUID.randomUUID().toString() + "." + UUID.randomUUID();
    String refreshHash = hashService.sha256Hex(refreshClair);
    RefreshToken rt = new RefreshToken();
    rt.setUtilisateur(utilisateur);
    rt.setTokenHash(refreshHash);
    rt.setExpireLe(Instant.now().plus(dureeRefresh));
    refreshTokenRepository.save(rt);

    long expiresIn = Duration.between(Instant.now(), jwtService.lireExpiration(access)).toSeconds();
    return new TokenResponse(access, refreshClair, expiresIn);
  }

  @Transactional
  public TokenResponse refresh(RefreshRequest request) {
    String refreshClair = request.refreshToken().trim();
    String refreshHash = hashService.sha256Hex(refreshClair);
    RefreshToken rt = refreshTokenRepository.findByTokenHash(refreshHash)
        .orElseThrow(() -> new ValidationException("Refresh token invalide"));
    if (rt.getRevoqueLe() != null) {
      throw new ValidationException("Refresh token révoqué");
    }
    if (rt.getExpireLe().isBefore(Instant.now())) {
      throw new ValidationException("Refresh token expiré");
    }

    Utilisateur utilisateur = rt.getUtilisateur();
    if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
      throw new ValidationException("Utilisateur inactif");
    }

    // rotation: révoque l'ancien, crée un nouveau
    rt.setRevoqueLe(Instant.now());

    Set<String> permissions = extrairePermissions(utilisateur);
    String access = jwtService.creerAccessToken(utilisateur, permissions);

    String nouveauRefreshClair = UUID.randomUUID().toString() + "." + UUID.randomUUID();
    String nouveauRefreshHash = hashService.sha256Hex(nouveauRefreshClair);
    RefreshToken nouveau = new RefreshToken();
    nouveau.setUtilisateur(utilisateur);
    nouveau.setTokenHash(nouveauRefreshHash);
    nouveau.setExpireLe(Instant.now().plus(dureeRefresh));
    refreshTokenRepository.save(nouveau);

    long expiresIn = Duration.between(Instant.now(), jwtService.lireExpiration(access)).toSeconds();
    return new TokenResponse(access, nouveauRefreshClair, expiresIn);
  }

  @Transactional
  public void logoutTout(Long utilisateurId) {
    refreshTokenRepository.revoquerTousPourUtilisateur(utilisateurId, Instant.now());
  }

  private Set<String> extrairePermissions(Utilisateur utilisateur) {
    return utilisateur.getRoles().stream()
        .flatMap((Role r) -> r.getPermissions().stream())
        .map(Permission::getCode)
        .collect(Collectors.toSet());
  }
}

