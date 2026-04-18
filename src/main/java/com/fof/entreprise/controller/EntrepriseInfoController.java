package com.fof.entreprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fof.entreprise.dto.EntrepriseInfoResponse;
import com.fof.entreprise.dto.ModifierEntrepriseInfoRequest;
import com.fof.entreprise.service.EntrepriseInfoService;
import jakarta.validation.ValidationException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/entreprise")
public class EntrepriseInfoController {

  private final EntrepriseInfoService service;
  private final ObjectMapper objectMapper;

  @GetMapping
  public EntrepriseInfoResponse obtenir() {
    return service.obtenir();
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('PERM_SETTINGS_MANAGE')")
  public EntrepriseInfoResponse modifier(
      @RequestPart("donnees") String donnees,
      @RequestPart(value = "logo", required = false) MultipartFile logo
  ) {
    try {
      ModifierEntrepriseInfoRequest req = objectMapper.readValue(donnees, ModifierEntrepriseInfoRequest.class);
      return service.modifier(req, logo);
    } catch (IOException e) {
      throw new ValidationException("donnees: JSON invalide");
    }
  }
}

