package com.fof.tresorerie.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transaction_tresorerie")
public class TransactionTresorerie {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TypeTransaction type;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montant;

  @Column(nullable = false)
  private Instant dateOperation = Instant.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "compte_source_id")
  private Compte compteSource;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "compte_destination_id")
  private Compte compteDestination;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TypeReference typeReference = TypeReference.MANUEL;

  @Column
  private Long idReference;

  @Column(length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutTransactionTresorerie statut = StatutTransactionTresorerie.POSTEE;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_origine_id")
  private TransactionTresorerie transactionOrigine;
  @Column(length = 255)
  private String motifAnnulation;

  @Column
  private Instant dateAnnulation;
}

