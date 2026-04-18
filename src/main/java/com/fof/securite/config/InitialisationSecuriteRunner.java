package com.fof.securite.config;

import com.fof.securite.service.InitialisationSecuriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class InitialisationSecuriteRunner {

  private final InitialisationSecuriteService initialisationSecuriteService;

  @Bean
  CommandLineRunner initialiserSecurite() {
    return args -> initialisationSecuriteService.initialiserSiBesoin();
  }
}

