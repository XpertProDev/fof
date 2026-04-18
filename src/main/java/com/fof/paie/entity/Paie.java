package com.fof.paie.entity;

import com.fof.employe.entity.Employe;
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
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "paie")
public class Paie {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employe_id", nullable = false)
  private Employe employe;

  @Column(nullable = false, length = 7)
  private String mois; // format: YYYY-MM (YearMonth)

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal salaireBase;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal primes = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal deductions = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal salaireNet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutPaie statut = StatutPaie.BROUILLON;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();

  public YearMonth getMoisYearMonth() {
    return YearMonth.parse(mois);
  }

  public void setMoisYearMonth(YearMonth ym) {
    this.mois = ym.toString();
  }
}

