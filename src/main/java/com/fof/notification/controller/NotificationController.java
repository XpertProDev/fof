package com.fof.notification.controller;

import com.fof.notification.dto.NotificationResponse;
import com.fof.notification.entity.StatutNotification;
import com.fof.notification.entity.TypeNotification;
import com.fof.notification.service.NotificationService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @PreAuthorize(
      "hasAuthority('PERM_ACCOUNTING_VIEW') or hasAuthority('PERM_SETTINGS_MANAGE') or hasAuthority('PERM_FACTURE_READ')"
  )
  public Page<NotificationResponse> lister(
      String recherche,
      TypeNotification type,
      StatutNotification statut,
      Instant debut,
      Instant fin,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return notificationService.lister(recherche, type, statut, debut, fin, pageable);
  }

  @PostMapping("/{id}/lue")
  @PreAuthorize(
      "hasAuthority('PERM_ACCOUNTING_VIEW') or hasAuthority('PERM_SETTINGS_MANAGE') or hasAuthority('PERM_FACTURE_READ')"
  )
  public NotificationResponse marquerLue(@PathVariable Long id) {
    return notificationService.marquerLue(id);
  }

  @PostMapping("/{id}/archiver")
  @PreAuthorize(
      "hasAuthority('PERM_ACCOUNTING_VIEW') or hasAuthority('PERM_SETTINGS_MANAGE') or hasAuthority('PERM_FACTURE_READ')"
  )
  public NotificationResponse archiver(@PathVariable Long id) {
    return notificationService.archiver(id);
  }
}

