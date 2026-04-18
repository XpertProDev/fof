package com.fof.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 160)
  private String nomComplet;

  @Column(length = 30)
  private String telephone;

  @Column(length = 160)
  private String email;

  @Column(length = 255)
  private String adresse;

  @Column(length = 80)
  private String pays;

  @Column(length = 255)
  private String photoUrl;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatutClient statut = StatutClient.ACTIF;
}

