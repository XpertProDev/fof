package com.fof.paie.entity;

import com.fof.tresorerie.entity.Compte;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "paiement_paie")
public class PaiementPaie {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "paie_id", nullable = false)
  private Paie paie;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montant;

  @Column(nullable = false)
  private Instant datePaiement = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "compte_source_id", nullable = false)
  private Compte compteSource;

  @Column(length = 255)
  private String commentaire;
}

