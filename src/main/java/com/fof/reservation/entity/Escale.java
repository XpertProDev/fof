package com.fof.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "escale_vol")
@Getter
@Setter
@NoArgsConstructor
public class Escale {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String villeDepart;

  @Column(nullable = false, length = 120)
  private String villeArrivee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vol_id", nullable = false)
  private Vol vol;
}
