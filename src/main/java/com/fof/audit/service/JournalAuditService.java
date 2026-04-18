package com.fof.audit.service;

import com.fof.audit.dto.JournalAuditResponse;
import com.fof.audit.entity.JournalAudit;
import com.fof.audit.repository.JournalAuditRepository;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class JournalAuditService {

  private final JournalAuditRepository journalAuditRepository;

  @Transactional
  public void enregistrer(String action, String typeEntite, Long idEntite, String details) {
    Objects.requireNonNull(action, "action");
    JournalAudit a = new JournalAudit();
    a.setUtilisateurId(utilisateurCourantId());
    a.setAction(action);
    a.setTypeEntite(typeEntite);
    a.setIdEntite(idEntite);
    a.setDetails(nettoyer(details));
    journalAuditRepository.save(a);
  }

  @Transactional(readOnly = true)
  public Page<JournalAuditResponse> lister(
      String recherche,
      Long utilisateurId,
      String action,
      String typeEntite,
      Instant debut,
      Instant fin,
      Pageable pageable
  ) {
    String q = (recherche == null || recherche.isBlank()) ? null : recherche.trim();
    String a = (action == null || action.isBlank()) ? null : action.trim();
    String t = (typeEntite == null || typeEntite.isBlank()) ? null : typeEntite.trim();
    return journalAuditRepository.rechercher(q, utilisateurId, a, t, debut, fin, pageable)
        .map(this::versResponse);
  }

  private JournalAuditResponse versResponse(JournalAudit a) {
    return new JournalAuditResponse(
        a.getId(),
        a.getUtilisateurId(),
        a.getAction(),
        a.getTypeEntite(),
        a.getIdEntite(),
        a.getHorodatage(),
        a.getDetails()
    );
  }

  private Long utilisateurCourantId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return null;
    }
    Object p = auth.getPrincipal();
    if (p instanceof Long l) {
      return l;
    }
    try {
      return Long.parseLong(String.valueOf(p));
    } catch (Exception e) {
      return null;
    }
  }

  private String nettoyer(String v) {
    if (v == null) return null;
    String s = v.trim();
    return s.isEmpty() ? null : s;
  }
}

