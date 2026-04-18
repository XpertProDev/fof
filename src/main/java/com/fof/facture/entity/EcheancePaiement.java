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
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "echeance_paiement")
public class EcheancePaiement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "facture_id", nullable = false)
  private Facture facture;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montantProgramme;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montantPaye = BigDecimal.ZERO;

  @Column(nullable = false)
  private LocalDate datePrevue;

  @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutEcheancePaiement statut = StatutEcheancePaiement.EN_ATTENTE;

  @Column(length = 255)
  private String commentaire;

  @Column
  private Instant datePaiement;
}

