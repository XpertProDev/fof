package com.fof.tresorerie.controller;

import com.fof.tresorerie.dto.DepotRequest;
import com.fof.tresorerie.dto.AnnulerTransactionRequest;
import com.fof.tresorerie.dto.RetraitRequest;
import com.fof.tresorerie.dto.FluxMoisRapportResponse;
import com.fof.tresorerie.dto.TransactionTresorerieResponse;
import com.fof.tresorerie.dto.TransfertRequest;
import com.fof.tresorerie.entity.TypeTransaction;
import com.fof.tresorerie.service.TransactionTresorerieService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionTresorerieController {

  private final TransactionTresorerieService transactionService;

  @PostMapping("/depot")
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE')")
  public TransactionTresorerieResponse depot(@Valid @RequestBody DepotRequest request) {
    return transactionService.depot(request);
  }

  @PostMapping("/retrait")
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE')")
  public TransactionTresorerieResponse retrait(@Valid @RequestBody RetraitRequest request) {
    return transactionService.retrait(request);
  }

  @PostMapping("/transfert")
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE')")
  public TransactionTresorerieResponse transfert(@Valid @RequestBody TransfertRequest request) {
    return transactionService.transfert(request);
  }

  @GetMapping("/rapport-mensuel")
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public List<FluxMoisRapportResponse> rapportMensuel(
      @RequestParam String debut,
      @RequestParam String fin
  ) {
    try {
      return transactionService.rapportMensuel(YearMonth.parse(debut), YearMonth.parse(fin));
    } catch (java.time.format.DateTimeParseException e) {
      throw new ValidationException("debut et fin doivent être au format YYYY-MM");
    }
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE') or hasAuthority('PERM_ACCOUNTING_VIEW')")
  public Page<TransactionTresorerieResponse> lister(
      TypeTransaction type,
      @RequestParam(required = false) String recherche,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate debut,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fin,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    return transactionService.listerTransactions(type, recherche, debut, fin, pageable);
  }

  @PostMapping("/{id}/annulation")
  @PreAuthorize("hasAuthority('PERM_TREASURY_MANAGE')")
  public TransactionTresorerieResponse annuler(@PathVariable Long id, @Valid @RequestBody AnnulerTransactionRequest request) {
    return transactionService.annuler(id, request.motif());
  }
}

