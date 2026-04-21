package com.fof.employe.controller;

import com.fof.employe.dto.CreerEmployeRequest;
import com.fof.employe.dto.EmployeResponse;
import com.fof.employe.dto.ModifierEmployeRequest;
import com.fof.employe.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/employes")
public class EmployeController {

  private final EmployeService employeService;

  @PostMapping
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE')")
  public EmployeResponse creer(@Valid @RequestBody CreerEmployeRequest request) {
    return employeService.creer(request);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE')")
  public EmployeResponse modifier(
      @PathVariable Long id,
      @Valid @RequestPart("donnees") ModifierEmployeRequest request,
      @RequestPart(value = "photo", required = false) MultipartFile photo,
      @RequestPart(value = "photoPiece", required = false) MultipartFile photoPiece
  ) {
    return employeService.modifier(id, request, photo, photoPiece);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE') or hasAuthority('PERM_PAYROLL_MANAGE')")
  public EmployeResponse detail(@PathVariable Long id) {
    return employeService.detail(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE') or hasAuthority('PERM_PAYROLL_MANAGE')")
  public Page<EmployeResponse> lister(String recherche, @PageableDefault(size = 20) Pageable pageable) {
    return employeService.lister(recherche, pageable);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('PERM_EMPLOYEE_MANAGE')")
  public void supprimer(@PathVariable Long id) {
    employeService.supprimer(id);
  }
}

