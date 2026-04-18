package com.fof.client.repository;

import com.fof.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

  @Query("""
      select c
      from Client c
      where lower(c.nomComplet) like lower(concat('%', :q, '%'))
         or lower(coalesce(c.telephone, '')) like lower(concat('%', :q, '%'))
         or lower(coalesce(c.email, '')) like lower(concat('%', :q, '%'))
      """)
  Page<Client> rechercher(@Param("q") String q, Pageable pageable);
}

