package com.fof.notification.dto;

import com.fof.notification.entity.StatutNotification;
import com.fof.notification.entity.TypeNotification;
import com.fof.tresorerie.entity.TypeReference;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    TypeNotification type,
    StatutNotification statut,
    String message,
    TypeReference typeReference,
    Long idReference,
    Instant dateCreation,
    Instant dateLecture,
    Instant dateArchivage
) {}

