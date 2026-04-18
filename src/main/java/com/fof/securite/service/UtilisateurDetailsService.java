package com.fof.securite.service;

import com.fof.securite.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UtilisateurDetailsService implements UserDetailsService {

  private final UtilisateurRepository utilisateurRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return utilisateurRepository.findByEmailIgnoreCase(username)
        .map(UtilisateurDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
  }
}

