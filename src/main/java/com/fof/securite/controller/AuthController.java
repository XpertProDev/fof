package com.fof.securite.controller;

import com.fof.securite.dto.LoginRequest;
import com.fof.securite.dto.RefreshRequest;
import com.fof.securite.dto.TokenResponse;
import com.fof.securite.service.AuthentificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthentificationService authentificationService;

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return authentificationService.login(request.email(), request.motDePasse());
  }

  @PostMapping("/refresh")
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authentificationService.refresh(request);
  }

  @PostMapping("/logout-tout")
  public void logoutTout(Authentication authentication) {
    Long utilisateurId = (Long) authentication.getPrincipal();
    authentificationService.logoutTout(utilisateurId);
  }
}

