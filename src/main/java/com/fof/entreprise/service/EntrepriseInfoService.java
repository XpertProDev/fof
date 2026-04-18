package com.fof.entreprise.service;

import com.fof.entreprise.dto.EntrepriseInfoResponse;
import com.fof.entreprise.dto.ModifierEntrepriseInfoRequest;
import com.fof.entreprise.entity.EntrepriseInfo;
import com.fof.entreprise.repository.EntrepriseInfoRepository;
import com.fof.fichier.service.StockageFichierService;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class EntrepriseInfoService {

  private static final long SINGLETON_ID = 1L;

  private final EntrepriseInfoRepository repository;
  private final StockageFichierService stockageFichierService;

  @Transactional(readOnly = true)
  public EntrepriseInfoResponse obtenir() {
    EntrepriseInfo info = repository.findById(SINGLETON_ID).orElseGet(this::creerParDefaut);
    return versResponse(info);
  }

  @Transactional
  public EntrepriseInfoResponse modifier(ModifierEntrepriseInfoRequest request, MultipartFile logo) {
    Objects.requireNonNull(request, "request");
    EntrepriseInfo info = repository.findById(SINGLETON_ID).orElseGet(this::creerParDefaut);

    if (request.nom() != null) info.setNom(nettoyer(request.nom()));
    if (request.telephone() != null) info.setTelephone(nettoyer(request.telephone()));
    if (request.email() != null) info.setEmail(nettoyer(request.email()));
    if (request.adresse() != null) info.setAdresse(nettoyer(request.adresse()));
    if (request.ville() != null) info.setVille(nettoyer(request.ville()));
    if (request.pays() != null) info.setPays(nettoyer(request.pays()));
    if (request.ninea() != null) info.setNinea(nettoyer(request.ninea()));
    if (request.rccm() != null) info.setRccm(nettoyer(request.rccm()));
    if (request.nomBanque() != null) info.setNomBanque(nettoyer(request.nomBanque()));
    if (request.numeroCompteBanque() != null) info.setNumeroCompteBanque(nettoyer(request.numeroCompteBanque()));

    if (info.getNom() == null || info.getNom().isBlank()) {
      info.setNom("Mon Entreprise");
    }

    if (logo != null && !logo.isEmpty()) {
      String ancienne = info.getLogoUrl();
      String url = stockageFichierService.enregistrerImage(logo, "entrepriseUpload");
      info.setLogoUrl(url);
      stockageFichierService.supprimerSiPossible(ancienne);
    }

    info.setDateMiseAJour(Instant.now());
    repository.save(info);
    return versResponse(info);
  }

  private EntrepriseInfo creerParDefaut() {
    EntrepriseInfo info = new EntrepriseInfo();
    info.setId(SINGLETON_ID);
    info.setNom("Mon Entreprise");
    info.setDateMiseAJour(Instant.now());
    return repository.save(info);
  }

  private EntrepriseInfoResponse versResponse(EntrepriseInfo e) {
    return new EntrepriseInfoResponse(
        e.getId(),
        e.getNom(),
        e.getTelephone(),
        e.getEmail(),
        e.getAdresse(),
        e.getVille(),
        e.getPays(),
        e.getNinea(),
        e.getRccm(),
        e.getNomBanque(),
        e.getNumeroCompteBanque(),
        e.getLogoUrl()
    );
  }

  private String nettoyer(String v) {
    if (v == null) return null;
    String t = v.trim();
    return t.isEmpty() ? null : t;
  }

  // Note: si tu veux “effacer” un champ, envoie une chaîne vide "" → ça devient null.
}

