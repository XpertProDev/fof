-- Anciennes factures restées en BROUILLON alors qu’elles ont un reste à payer : passer en statut « émise » / retard / payée.

SET NAMES utf8mb4;

UPDATE facture
SET statut = CASE
  WHEN montant_restant <= 0 THEN 'PAYEE'
  WHEN date_echeance IS NOT NULL AND date_echeance < CURDATE() THEN 'EN_RETARD'
  ELSE 'ENVOYEE'
END
WHERE statut = 'BROUILLON'
  AND montant_restant IS NOT NULL;
