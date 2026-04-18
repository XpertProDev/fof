package com.fof.depense.entity;

import com.fof.tresorerie.entity.Compte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "depense")
public class Depense {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 160)
  private String titre;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montant;

  @Column(length = 80)
  private String categorie;

  @Column(nullable = false)
  private LocalDate dateDepense = LocalDate.now();

  @Column(length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatutDepense statut = StatutDepense.BROUILLON;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "compte_paiement_id")
  private Compte comptePaiement;

  @Column
  private Instant datePaiement;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();
}

