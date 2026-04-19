package com.fof.tresorerie.dto;

import com.fof.tresorerie.entity.StatutTransactionTresorerie;
import com.fof.tresorerie.entity.TypeTransaction;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mouvement de trésorerie aligné sur le contrat page Comptabilité (libellé, noms de comptes, référence).
 */
public record TransactionTresorerieResponse(
    Long id,
    Instant date,
    TypeTransaction type,
    String libelle,
    BigDecimal montant,
    String devise,
    /** ENTREE / SORTIE : compte impacté (affichage tableau). */
    String compteNom,
    /** TRANSFERT : nom du compte débité. */
    String compteSource,
    /** TRANSFERT : nom du compte crédité. */
    String compteDestination,
    /** Ex. {@code FACTURE:12} ou {@code null} si manuel sans référence. */
    String reference,
    StatutTransactionTresorerie statut
) {}
