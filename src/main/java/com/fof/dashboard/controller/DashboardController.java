package com.fof.dashboard.controller;

import com.fof.dashboard.dto.DashboardResponse;
import com.fof.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_ACCOUNTING_VIEW')")
  public DashboardResponse charger(
      @RequestParam(required = false) Integer annee,
      @RequestParam(required = false) Integer tailleOperationsRecentes
  ) {
    return dashboardService.chargerDashboard(annee, tailleOperationsRecentes);
  }
}

