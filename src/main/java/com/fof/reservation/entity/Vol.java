package com.fof.reservation.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vol_reservation")
@Getter
@Setter
@NoArgsConstructor
public class Vol {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String numeroVol;

  @Column(nullable = false, length = 120)
  private String compagnie;

  @Column(nullable = false, length = 40)
  private String classeVoyage;

  @Column(length = 120)
  private String avion;

  @Column(length = 40)
  private String franchiseBagage;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "ville", column = @Column(name = "depart_ville", nullable = false, length = 120)),
      @AttributeOverride(name = "aeroport", column = @Column(name = "depart_aeroport", nullable = false, length = 120)),
      @AttributeOverride(name = "terminal", column = @Column(name = "depart_terminal", length = 20)),
      @AttributeOverride(name = "dateHeure", column = @Column(name = "depart_date_heure", nullable = false))
  })
  private AeroportInfo depart;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "ville", column = @Column(name = "arrivee_ville", nullable = false, length = 120)),
      @AttributeOverride(name = "aeroport", column = @Column(name = "arrivee_aeroport", nullable = false, length = 120)),
      @AttributeOverride(name = "terminal", column = @Column(name = "arrivee_terminal", length = 20)),
      @AttributeOverride(name = "dateHeure", column = @Column(name = "arrivee_date_heure", nullable = false))
  })
  private AeroportInfo arrivee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id", nullable = false)
  private Reservation reservation;

  @OneToMany(mappedBy = "vol", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "escale_ordre")
  @BatchSize(size = 32)
  private List<Escale> escales = new ArrayList<>();
}
