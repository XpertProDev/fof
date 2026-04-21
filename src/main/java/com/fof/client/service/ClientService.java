package com.fof.client.service;

import com.fof.client.dto.ClientResponse;
import com.fof.client.dto.CreerClientRequest;
import com.fof.client.dto.ModifierClientRequest;
import com.fof.client.entity.Client;
import com.fof.client.entity.StatutClient;
import com.fof.client.repository.ClientRepository;
import com.fof.fichier.service.StockageFichierService;
import com.fof.facture.repository.FactureRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final StockageFichierService stockageFichierService;
  private final FactureRepository factureRepository;

  @Transactional
  public ClientResponse creer(CreerClientRequest request) {
    Objects.requireNonNull(request, "request");
    Client client = new Client();
    appliquer(client, request.nomComplet(), request.telephone(), request.email(), request.adresse(), request.pays(), request.statut());
    Client sauvegarde = clientRepository.save(client);
    return versResponse(sauvegarde);
  }

  @Transactional
  public ClientResponse modifier(Long id, ModifierClientRequest request, MultipartFile photo) {
    Objects.requireNonNull(request, "request");
    Client client = charger(id);
    appliquer(client, request.nomComplet(), request.telephone(), request.email(), request.adresse(), request.pays(), request.statut());

    if (photo != null && !photo.isEmpty()) {
      String ancienne = client.getPhotoUrl();
      String url = stockageFichierService.enregistrerImage(photo, "clientUpload");
      client.setPhotoUrl(url);
      stockageFichierService.supprimerSiPossible(ancienne);
    }

    return versResponse(client);
  }

  @Transactional
  public void supprimer(Long id) {
    Client client = charger(id);
    long nbFactures = factureRepository.compterFacturesPourClientHorsAnnulees(id);
    if (nbFactures > 0) {
      throw new ValidationException("Impossible de supprimer ce client : " + nbFactures + " facture(s) liée(s) existent");
    }
    String anciennePhoto = client.getPhotoUrl();
    clientRepository.delete(client);
    stockageFichierService.supprimerSiPossible(anciennePhoto);
  }

  @Transactional(readOnly = true)
  public ClientResponse detail(Long id) {
    return versResponse(charger(id));
  }

  @Transactional(readOnly = true)
  public Page<ClientResponse> lister(String recherche, Pageable pageable) {
    if (recherche == null || recherche.isBlank()) {
      return clientRepository.findAll(pageable).map(this::versResponse);
    }
    return clientRepository.rechercher(recherche.trim(), pageable).map(this::versResponse);
  }

  @Transactional(readOnly = true)
  public Client charger(Long id) {
    return clientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client introuvable: " + id));
  }

  private void appliquer(
      Client client,
      String nomComplet,
      String telephone,
      String email,
      String adresse,
      String pays,
      StatutClient statut
  ) {
    client.setNomComplet(nomComplet.trim());
    client.setTelephone(nettoyer(telephone));
    client.setEmail(nettoyer(email));
    client.setAdresse(nettoyer(adresse));
    client.setPays(nettoyer(pays));
    client.setStatut(statut == null ? StatutClient.ACTIF : statut);
  }

  private String nettoyer(String valeur) {
    if (valeur == null) {
      return null;
    }
    String v = valeur.trim();
    return v.isEmpty() ? null : v;
  }

  private ClientResponse versResponse(Client c) {
    return new ClientResponse(
        c.getId(),
        c.getNomComplet(),
        c.getTelephone(),
        c.getEmail(),
        c.getAdresse(),
        c.getPays(),
        c.getPhotoUrl(),
        c.getDateCreation(),
        c.getStatut()
    );
  }
}

