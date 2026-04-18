package com.fof.audit.repository;

import com.fof.audit.entity.JournalAudit;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

  @Query("""
      select a
      from JournalAudit a
      where (:utilisateurId is null or a.utilisateurId = :utilisateurId)
        and (:action is null or lower(a.action) like lower(concat('%', :action, '%')))
        and (:typeEntite is null or lower(coalesce(a.typeEntite, '')) like lower(concat('%', :typeEntite, '%')))
        and (:debut is null or a.horodatage >= :debut)
        and (:fin is null or a.horodatage <= :fin)
        and (
          :q is null
          or lower(coalesce(a.details, '')) like lower(concat('%', :q, '%'))
        )
      order by a.horodatage desc
      """)
  Page<JournalAudit> rechercher(
      @Param("q") String q,
      @Param("utilisateurId") Long utilisateurId,
      @Param("action") String action,
      @Param("typeEntite") String typeEntite,
      @Param("debut") Instant debut,
      @Param("fin") Instant fin,
      Pageable pageable
  );
}

