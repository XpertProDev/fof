package com.fof.dashboard.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public record DashboardResponse(
    String devise,
    BigDecimal chiffreAffairesTotal,
    BigDecimal dettesClients,
    long nombreFacturesEnAttente,
    BigDecimal depensesDuMois,
    BigDecimal salairesAPayer,
    BigDecimal soldeGlobal,
    List<SoldeCompteResponse> soldesParCompte,
    List<MoisTotalResponse> chiffreAffairesParMois,
    List<MoisTotalResponse> depensesParMois,
    List<FluxParMoisResponse> fluxParMois,
    List<OperationRecenteResponse> operationsRecentes,
    Map<String, Long> facturesParStatut
) {}

