package com.fof.securite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String nom;

  @Column(nullable = false, length = 80)
  private String prenom;

  @Column(length = 30)
  private String telephone;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 255)
  private String motDePasseHash;

  @Column(length = 255)
  private String photoUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutUtilisateur statut = StatutUtilisateur.ACTIF;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "utilisateur_role",
      joinColumns = @JoinColumn(name = "utilisateur_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();
}

