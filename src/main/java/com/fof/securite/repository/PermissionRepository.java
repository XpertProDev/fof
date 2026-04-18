package com.fof.securite.repository;

import com.fof.securite.entity.Permission;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
  Optional<Permission> findByCodeIgnoreCase(String code);

  @Query("""
      select p
      from Permission p
      where :q is null
         or lower(p.code) like lower(concat('%', :q, '%'))
         or lower(coalesce(p.libelle, '')) like lower(concat('%', :q, '%'))
      order by p.code asc
      """)
  Page<Permission> rechercher(@Param("q") String q, Pageable pageable);
}

