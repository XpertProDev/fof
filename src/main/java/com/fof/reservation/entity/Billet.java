package com.fof.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billet_reservation")
@Getter
@Setter
@NoArgsConstructor
public class Billet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Fourni par un système externe — jamais généré par l'application. */
  @Column(name = "numero_billet", nullable = false, unique = true, length = 80)
  private String numeroBillet;

  @Column(nullable = false, length = 40)
  private String type;

  @Column(nullable = false, length = 120)
  private String compagnieEmission;
}
