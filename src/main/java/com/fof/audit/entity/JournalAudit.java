package com.fof.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "journal_audit",
    indexes = {
        @Index(name = "idx_audit_horodatage", columnList = "horodatage"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_type_entite", columnList = "typeEntite"),
        @Index(name = "idx_audit_utilisateur_id", columnList = "utilisateurId")
    }
)
public class JournalAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private Long utilisateurId;

  @Column(nullable = false, length = 80)
  private String action;

  @Column(length = 60)
  private String typeEntite;

  @Column
  private Long idEntite;

  @Column(nullable = false)
  private Instant horodatage = Instant.now();

  @Column(length = 2000)
  private String details;
}

