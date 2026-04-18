package com.fof.securite.controller;

import com.fof.securite.dto.ModifierProfilRequest;
import com.fof.securite.dto.ProfilUtilisateurResponse;
import com.fof.securite.service.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/profil")
public class ProfilController {

  private final ProfilService profilService;

  @GetMapping
  public ProfilUtilisateurResponse moi() {
    return profilService.moi();
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProfilUtilisateurResponse modifierMoi(
      @Valid @RequestPart("donnees") ModifierProfilRequest donnees,
      @RequestPart(value = "photo", required = false) MultipartFile photo
  ) {
    return profilService.modifierMoi(donnees, photo);
  }
}

