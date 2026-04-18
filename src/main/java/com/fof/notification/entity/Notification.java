package com.fof.notification.entity;

import com.fof.tresorerie.entity.TypeReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "notification",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notification_dedup",
            columnNames = {"type", "typeReference", "idReference", "cleDedup"}
        )
    },
    indexes = {
        @Index(name = "idx_notification_statut", columnList = "statut"),
        @Index(name = "idx_notification_date", columnList = "dateCreation"),
        @Index(name = "idx_notification_type", columnList = "type")
    }
)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private TypeNotification type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatutNotification statut = StatutNotification.NOUVELLE;

  @Column(nullable = false, length = 255)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TypeReference typeReference = TypeReference.MANUEL;

  @Column
  private Long idReference;

  @Column(nullable = false, length = 60)
  private String cleDedup;

  @Column(nullable = false)
  private Instant dateCreation = Instant.now();

  @Column
  private Instant dateLecture;

  @Column
  private Instant dateArchivage;
}

