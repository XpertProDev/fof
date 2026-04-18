package com.fof.securite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private Utilisateur utilisateur;

  @Column(nullable = false, unique = true, length = 120)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expireLe;

  @Column(nullable = false)
  private Instant creeLe = Instant.now();

  @Column
  private Instant revoqueLe;
}

