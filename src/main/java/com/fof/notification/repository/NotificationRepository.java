package com.fof.notification.repository;

import com.fof.notification.entity.Notification;
import com.fof.notification.entity.StatutNotification;
import com.fof.notification.entity.TypeNotification;
import com.fof.tresorerie.entity.TypeReference;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// public interface NotificationRepository extends JpaRepository<Notification, Long> {

//   Optional<Notification> findByTypeAndTypeReferenceAndIdReferenceAndCleDedup(
//       TypeNotification type,
//       TypeReference typeReference,
//       Long idReference,
//       String cleDedup
//   );

//   @Query("""
//       select n
//       from Notification n
//       where (:type is null or n.type = :type)
//         and (:statut is null or n.statut = :statut)
//         and (:debut is null or n.dateCreation >= :debut)
//         and (:fin is null or n.dateCreation <= :fin)
//         and (
//           :q is null
//           or lower(n.message) like lower(concat('%', :q, '%'))
//         )
//       order by n.dateCreation desc
//       """)
//   Page<Notification> rechercher(
//       @Param("q") String q,
//       @Param("type") TypeNotification type,
//       @Param("statut") StatutNotification statut,
//       @Param("debut") Instant debut,
//       @Param("fin") Instant fin,
//       Pageable pageable
//   );
// }

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByTypeAndTypeReferenceAndIdReferenceAndCleDedup(
      TypeNotification type,
      TypeReference typeReference,
      Long idReference,
      String cleDedup
  );

  @Query("""
      select n
      from Notification n
      where (:type is null or n.type = :type)
        and (:statut is null or n.statut = :statut)
        and (:debut is null or n.dateCreation >= CAST(:debut AS timestamp))
        and (:fin is null or n.dateCreation <= CAST(:fin AS timestamp))
        and (
          :q is null
          or lower(n.message) like lower(concat('%', :q, '%'))
        )
      order by n.dateCreation desc
      """)
  Page<Notification> rechercher(
      @Param("q") String q,
      @Param("type") TypeNotification type,
      @Param("statut") StatutNotification statut,
      @Param("debut") Instant debut,
      @Param("fin") Instant fin,
      Pageable pageable
  );
}