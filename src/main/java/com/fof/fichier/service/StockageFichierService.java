package com.fof.fichier.service;

import jakarta.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StockageFichierService {

  private final Path racineStockage;

  public StockageFichierService(@Value("${app.stockage.racine:src/main/resources/static}") String racineStockage) {
    this.racineStockage = Paths.get(racineStockage).toAbsolutePath().normalize();
  }

  public String enregistrerImage(MultipartFile fichier, String dossier) {
    if (fichier == null || fichier.isEmpty()) {
      throw new ValidationException("Fichier image vide");
    }
    String contentType = fichier.getContentType();
    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
      throw new ValidationException("Le fichier doit être une image");
    }

    String nomOriginal = fichier.getOriginalFilename() == null ? "image" : fichier.getOriginalFilename();
    String nomNettoye = nomOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
    String nomFichier = UUID.randomUUID() + "_" + nomNettoye;

    Path dossierPath = racineStockage.resolve(dossier).normalize();
    try {
      Files.createDirectories(dossierPath);
      Path destination = dossierPath.resolve(nomFichier).normalize();
      Files.copy(fichier.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ValidationException("Erreur lors de l'enregistrement de l'image");
    }

    return "/" + dossier + "/" + nomFichier;
  }

  public void supprimerSiPossible(String url) {
    if (url == null || url.isBlank()) return;
    // On supprime uniquement les fichiers sous la racine static (ex: /userUpload/x.png)
    String relatif = url.startsWith("/") ? url.substring(1) : url;
    Path cible = racineStockage.resolve(relatif).normalize();
    if (!cible.startsWith(racineStockage)) {
      return;
    }
    try {
      Files.deleteIfExists(cible);
    } catch (IOException ignored) {
    }
  }

  public Path getRacineStockage() {
    return racineStockage;
  }
}

