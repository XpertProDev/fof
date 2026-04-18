package com.fof.securite.repository;

import com.fof.securite.entity.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByNomIgnoreCase(String nom);

  @Query("""
      select r
      from Role r
      where :q is null
         or lower(r.nom) like lower(concat('%', :q, '%'))
      """)
  Page<Role> rechercher(@Param("q") String q, Pageable pageable);
}

