package com.fof.securite.service;

import com.fof.securite.entity.Utilisateur;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey cle;
  private final Duration dureeAccess;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-minutes:15}") long accessMinutes
  ) {
    // secret doit être suffisamment long; ici on accepte string et on la passe en bytes
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.cle = Keys.hmacShaKeyFor(keyBytes);
    this.dureeAccess = Duration.ofMinutes(accessMinutes);
  }

  public String creerAccessToken(Utilisateur utilisateur, Set<String> permissions) {
    Instant maintenant = Instant.now();
    Instant expiration = maintenant.plus(dureeAccess);
    return Jwts.builder()
        .subject(utilisateur.getId().toString())
        .issuedAt(Date.from(maintenant))
        .expiration(Date.from(expiration))
        .claim("email", utilisateur.getEmail())
        .claim("permissions", permissions)
        .signWith(cle, Jwts.SIG.HS256)
        .compact();
  }

  public Instant lireExpiration(String jwt) {
    return parser(jwt).getExpiration().toInstant();
  }

  public Long lireUtilisateurId(String jwt) {
    return Long.parseLong(parser(jwt).getSubject());
  }

  public Set<String> lirePermissions(String jwt) {
    Object brut = parser(jwt).get("permissions");
    if (brut == null) {
      return Set.of();
    }
    if (brut instanceof List<?> liste) {
      return liste.stream().map(String::valueOf).collect(Collectors.toSet());
    }
    return Set.of(String.valueOf(brut));
  }

  private io.jsonwebtoken.Claims parser(String jwt) {
    return Jwts.parser()
        .verifyWith(cle)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();
  }
}

