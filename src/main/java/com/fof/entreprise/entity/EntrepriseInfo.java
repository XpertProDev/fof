package com.fof.entreprise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "entreprise_info")
public class EntrepriseInfo {

  /**
   * Singleton: on stocke une seule ligne (id=1) pour l’entreprise courante.
   */
  @Id
  private Long id = 1L;

  @Column(nullable = false, length = 160)
  private String nom;

  @Column(length = 120)
  private String telephone;

  @Column(length = 120)
  private String email;

  @Column(length = 255)
  private String adresse;

  @Column(length = 80)
  private String ville;

  @Column(length = 80)
  private String pays;

  @Column(length = 80)
  private String ninea;

  @Column(length = 80)
  private String rccm;

  @Column(length = 120)
  private String nomBanque;

  @Column(length = 120)
  private String numeroCompteBanque;

  /**
   * URL publique du logo (ex: /entrepriseUpload/logo.png).
   */
  @Column(length = 255)
  private String logoUrl;

  /**
   * Texte libre à afficher en bas des factures (RIB, mentions légales, etc.).
   */
  @Column(length = 1000)
  private String piedDePage;

  @Column(nullable = false)
  private Instant dateMiseAJour = Instant.now();
}

