package com.fof.securite.repository;

import com.fof.securite.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying
  @Query("update RefreshToken rt set rt.revoqueLe = :now where rt.utilisateur.id = :utilisateurId and rt.revoqueLe is null")
  int revoquerTousPourUtilisateur(@Param("utilisateurId") Long utilisateurId, @Param("now") Instant now);

  @Modifying
  @Query("delete from RefreshToken rt where rt.utilisateur.id = :utilisateurId")
  int supprimerTousPourUtilisateur(@Param("utilisateurId") Long utilisateurId);
}

