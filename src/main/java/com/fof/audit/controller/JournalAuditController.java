package com.fof.audit.controller;

import com.fof.audit.dto.JournalAuditResponse;
import com.fof.audit.service.JournalAuditService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/audits")
public class JournalAuditController {

  private final JournalAuditService journalAuditService;

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_ACCOUNTING_VIEW') or hasAuthority('PERM_SETTINGS_MANAGE')")
  public Page<JournalAuditResponse> lister(
      String recherche,
      Long utilisateurId,
      String action,
      String typeEntite,
      Instant debut,
      Instant fin,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return journalAuditService.lister(recherche, utilisateurId, action, typeEntite, debut, fin, pageable);
  }
}

