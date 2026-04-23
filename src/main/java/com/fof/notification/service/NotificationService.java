package com.fof.notification.service;

import com.fof.notification.dto.NotificationResponse;
import com.fof.notification.entity.Notification;
import com.fof.notification.entity.StatutNotification;
import com.fof.notification.entity.TypeNotification;
import com.fof.notification.repository.NotificationRepository;
import com.fof.tresorerie.entity.TypeReference;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public void creerSiAbsente(
      TypeNotification type,
      String message,
      TypeReference typeReference,
      Long idReference,
      String cleDedup
  ) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(message, "message");
    Objects.requireNonNull(typeReference, "typeReference");
    Objects.requireNonNull(cleDedup, "cleDedup");

    // Dédoublonnage (best-effort) : si déjà une notification active, on ne recrée pas.
    // Si elle est archivée, on la réouvre (même clé = contrainte unique) pour qu’elle réapparaisse dans la liste.
    if (idReference != null) {
      var existante = notificationRepository.findByTypeAndTypeReferenceAndIdReferenceAndCleDedup(type, typeReference, idReference, cleDedup);
      if (existante.isPresent()) {
        Notification e = existante.get();
        if (e.getStatut() == StatutNotification.NOUVELLE || e.getStatut() == StatutNotification.LUE) {
          return;
        }
        if (e.getStatut() == StatutNotification.ARCHIVEE) {
          e.setStatut(StatutNotification.NOUVELLE);
          e.setMessage(message.trim());
          e.setDateLecture(null);
          e.setDateArchivage(null);
          notificationRepository.save(e);
        }
        return;
      }
    }

    Notification n = new Notification();
    n.setType(type);
    n.setMessage(message.trim());
    n.setTypeReference(typeReference);
    n.setIdReference(idReference);
    n.setCleDedup(cleDedup.trim());
    try {
      notificationRepository.save(n);
    } catch (DataIntegrityViolationException e) {
      // collision dedup -> ok
    }
  }

  @Transactional(readOnly = true)
  public Page<NotificationResponse> lister(
      String recherche,
      TypeNotification type,
      StatutNotification statut,
      Instant debut,
      Instant fin,
      Pageable pageable
  ) {
      String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
      
      String debutStr = debut != null ? debut.toString() : null;
      String finStr = fin != null ? fin.toString() : null;
      
      return notificationRepository.rechercher(q, type, statut, debutStr, finStr, pageable)
          .map(this::versResponse);
  }

  @Transactional
  public NotificationResponse marquerLue(Long id) {
    Notification n = charger(id);
    if (n.getStatut() == StatutNotification.ARCHIVEE) {
      return versResponse(n);
    }
    n.setStatut(StatutNotification.LUE);
    n.setDateLecture(Instant.now());
    return versResponse(n);
  }

  @Transactional
  public NotificationResponse archiver(Long id) {
    Notification n = charger(id);
    n.setStatut(StatutNotification.ARCHIVEE);
    n.setDateArchivage(Instant.now());
    return versResponse(n);
  }

  private Notification charger(Long id) {
    return notificationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Notification introuvable: " + id));
  }

  private NotificationResponse versResponse(Notification n) {
    return new NotificationResponse(
        n.getId(),
        n.getType(),
        n.getStatut(),
        n.getMessage(),
        n.getTypeReference(),
        n.getIdReference(),
        n.getDateCreation(),
        n.getDateLecture(),
        n.getDateArchivage()
    );
  }
}

