package com.fof.securite.repository;

import com.fof.securite.entity.Utilisateur;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
  Optional<Utilisateur> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);

  @Query("""
      select u
      from Utilisateur u
      where :q is null
         or lower(u.nom) like lower(concat('%', :q, '%'))
         or lower(u.prenom) like lower(concat('%', :q, '%'))
         or lower(u.email) like lower(concat('%', :q, '%'))
      """)
  Page<Utilisateur> rechercher(@Param("q") String q, Pageable pageable);
}

