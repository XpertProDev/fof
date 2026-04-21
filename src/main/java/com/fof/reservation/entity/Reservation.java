package com.fof.reservation.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Référence métier — jamais générée par l'application. */
  @Column(name = "reference_reservation", nullable = false, unique = true, length = 80)
  private String referenceReservation;

  @Column(name = "date_reservation", nullable = false)
  private LocalDate dateReservation;

  @Column(nullable = false, length = 200)
  private String agence;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "passager_id", nullable = false)
  private Passager passager;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "billet_id", nullable = false)
  private Billet billet;

  @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "vol_ordre")
  @BatchSize(size = 16)
  private List<Vol> vols = new ArrayList<>();
}
