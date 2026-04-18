package com.fof.employe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employe")
public class Employe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String nom;

  @Column(nullable = false, length = 80)
  private String prenom;

  @Column(length = 30)
  private String telephone;

  @Column(length = 80)
  private String fonction;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal salaireBase;

  @Column(length = 50)
  private String typeContrat;

  @Column(length = 255)
  private String photoUrl;

  @Column(nullable = false)
  private LocalDate dateEmbauche = LocalDate.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutEmploye statut = StatutEmploye.ACTIF;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();
}

