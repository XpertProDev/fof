package com.fof.facture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ligne_facture")
public class LigneFacture {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "facture_id", nullable = false)
  private Facture facture;

  @Column(nullable = false, length = 255)
  private String description;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal quantite;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal prixUnitaire;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalLigne;
}

