package com.fof.employe.controller;

import com.fof.employe.dto.EmployeResponse;
import com.fof.employe.entity.Employe;
import com.fof.employe.service.EmployeService;
import com.fof.fichier.service.StockageFichierService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/employes")
public class EmployePhotoController {

  private final EmployeService employeService;
  private final StockageFichierService stockageFichierService;

  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE')")
  public EmployeResponse modifierPhoto(@PathVariable Long id, @RequestPart("photo") MultipartFile photo) {
    if (photo == null || photo.isEmpty()) {
      throw new ValidationException("Photo obligatoire");
    }
    Employe e = employeService.charger(id);
    String ancienne = e.getPhotoUrl();
    String url = stockageFichierService.enregistrerImage(photo, "employeUpload");
    e.setPhotoUrl(url);
    stockageFichierService.supprimerSiPossible(ancienne);
    return new EmployeResponse(
        e.getId(),
        e.getNom(),
        e.getPrenom(),
        e.getTelephone(),
        e.getFonction(),
        e.getSalaireBase(),
        e.getTypeContrat(),
        e.getPhotoUrl(),
        e.getDateEmbauche(),
        e.getStatut(),
        e.getDateCreation()
    );
  }
}

