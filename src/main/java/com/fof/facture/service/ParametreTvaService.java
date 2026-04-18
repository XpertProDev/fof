package com.fof.facture.service;

import java.math.BigDecimal;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class ParametreTvaService {

  private final boolean tvaActivee;
  private final BigDecimal tauxTvaParDefaut;

  public ParametreTvaService(
      @Value("${app.tva.activee:true}") boolean tvaActivee,
      @Value("${app.tva.taux-par-defaut:0.18}") BigDecimal tauxTvaParDefaut
  ) {
    this.tvaActivee = tvaActivee;
    this.tauxTvaParDefaut = tauxTvaParDefaut;
  }
}

