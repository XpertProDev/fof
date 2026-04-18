package com.fof.employe.repository;

import com.fof.employe.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

  @Query("""
      select e
      from Employe e
      where :q is null
         or lower(e.nom) like lower(concat('%', :q, '%'))
         or lower(e.prenom) like lower(concat('%', :q, '%'))
         or lower(coalesce(e.telephone, '')) like lower(concat('%', :q, '%'))
      """)
  Page<Employe> rechercher(@Param("q") String q, Pageable pageable);
}

