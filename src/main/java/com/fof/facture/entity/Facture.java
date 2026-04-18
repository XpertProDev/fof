package com.fof.facture.entity;

import com.fof.client.entity.Client;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "facture")
public class Facture {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 40)
  private String numero = "TEMP-" + System.currentTimeMillis();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Column(nullable = false)
  private LocalDate dateEmission = LocalDate.now();

  @Column
  private LocalDate dateEcheance;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  /** Par défaut « émise » ; le service affine EN_RETARD / PAYEE après calcul des totaux. */
  private StatutFacture statut = StatutFacture.ENVOYEE;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ModeTva modeTva = ModeTva.DESACTIVEE;

  @Column(precision = 6, scale = 4)
  private BigDecimal tauxTva;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalHt = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montantTva = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalTtc = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montantPaye = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montantRestant = BigDecimal.ZERO;

  @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<LigneFacture> lignes = new ArrayList<>();

  @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PaiementFacture> paiements = new ArrayList<>();

  @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<EcheancePaiement> echeancesPaiement = new ArrayList<>();
}

