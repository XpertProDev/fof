-- MySQL schema init (aligné avec les entités actuelles)
-- Engine/charset recommandés

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- Sécurité (RBAC + tokens)
-- =========================

CREATE TABLE IF NOT EXISTS permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(80) NOT NULL,
  libelle VARCHAR(160) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nom VARCHAR(60) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_role_permission_permission (permission_id),
  CONSTRAINT fk_role_permission_role
    FOREIGN KEY (role_id) REFERENCES role(id),
  CONSTRAINT fk_role_permission_permission
    FOREIGN KEY (permission_id) REFERENCES permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS utilisateur (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nom VARCHAR(80) NOT NULL,
  prenom VARCHAR(80) NOT NULL,
  telephone VARCHAR(30) NULL,
  email VARCHAR(160) NOT NULL,
  mot_de_passe_hash VARCHAR(255) NOT NULL,
  statut VARCHAR(15) NOT NULL,
  date_creation DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_utilisateur_email (email),
  KEY idx_utilisateur_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS utilisateur_role (
  utilisateur_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (utilisateur_id, role_id),
  KEY idx_utilisateur_role_role (role_id),
  CONSTRAINT fk_utilisateur_role_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
  CONSTRAINT fk_utilisateur_role_role
    FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_token (
  id BIGINT NOT NULL AUTO_INCREMENT,
  utilisateur_id BIGINT NOT NULL,
  token_hash VARCHAR(120) NOT NULL,
  expire_le DATETIME(6) NOT NULL,
  cree_le DATETIME(6) NOT NULL,
  revoque_le DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  KEY idx_refresh_token_utilisateur (utilisateur_id),
  KEY idx_refresh_token_expire_le (expire_le),
  CONSTRAINT fk_refresh_token_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Référentiels
-- =========================

CREATE TABLE IF NOT EXISTS client (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nom_complet VARCHAR(160) NOT NULL,
  telephone VARCHAR(30) NULL,
  email VARCHAR(160) NULL,
  adresse VARCHAR(255) NULL,
  pays VARCHAR(80) NULL,
  date_creation DATETIME(6) NOT NULL,
  statut VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_client_nom (nom_complet),
  KEY idx_client_telephone (telephone),
  KEY idx_client_email (email),
  KEY idx_client_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employe (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nom VARCHAR(80) NOT NULL,
  prenom VARCHAR(80) NOT NULL,
  telephone VARCHAR(30) NULL,
  fonction VARCHAR(80) NULL,
  salaire_base DECIMAL(19,2) NOT NULL,
  type_contrat VARCHAR(50) NULL,
  date_embauche DATE NOT NULL,
  statut VARCHAR(15) NOT NULL,
  date_creation DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_employe_nom (nom),
  KEY idx_employe_prenom (prenom),
  KEY idx_employe_telephone (telephone),
  KEY idx_employe_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Trésorerie (comptes + ledger)
-- =========================

CREATE TABLE IF NOT EXISTS compte (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nom VARCHAR(120) NOT NULL,
  type VARCHAR(30) NOT NULL,
  solde_actuel DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_compte_nom (nom),
  KEY idx_compte_type (type),
  KEY idx_compte_solde (solde_actuel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transaction_tresorerie (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(20) NOT NULL,
  montant DECIMAL(19,2) NOT NULL,
  date_operation DATETIME(6) NOT NULL,
  compte_source_id BIGINT NULL,
  compte_destination_id BIGINT NULL,
  type_reference VARCHAR(20) NOT NULL,
  id_reference BIGINT NULL,
  description VARCHAR(255) NULL,
  statut VARCHAR(15) NOT NULL,
  transaction_origine_id BIGINT NULL,
  motif_annulation VARCHAR(255) NULL,
  date_annulation DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_tx_date (date_operation),
  KEY idx_tx_type (type),
  KEY idx_tx_statut (statut),
  KEY idx_tx_ref (type_reference, id_reference),
  KEY idx_tx_compte_source (compte_source_id),
  KEY idx_tx_compte_destination (compte_destination_id),
  KEY idx_tx_origine (transaction_origine_id),
  CONSTRAINT fk_tx_compte_source
    FOREIGN KEY (compte_source_id) REFERENCES compte(id),
  CONSTRAINT fk_tx_compte_destination
    FOREIGN KEY (compte_destination_id) REFERENCES compte(id),
  CONSTRAINT fk_tx_transaction_origine
    FOREIGN KEY (transaction_origine_id) REFERENCES transaction_tresorerie(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Facturation
-- =========================

CREATE TABLE IF NOT EXISTS facture (
  id BIGINT NOT NULL AUTO_INCREMENT,
  numero VARCHAR(40) NOT NULL,
  client_id BIGINT NOT NULL,
  date_emission DATE NOT NULL,
  date_echeance DATE NULL,
  date_creation DATETIME(6) NOT NULL,
  statut VARCHAR(25) NOT NULL,
  mode_tva VARCHAR(20) NOT NULL,
  taux_tva DECIMAL(6,4) NULL,
  total_ht DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  montant_tva DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  total_ttc DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  montant_paye DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  montant_restant DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (id),
  UNIQUE KEY uk_facture_numero (numero),
  KEY idx_facture_client (client_id),
  KEY idx_facture_statut (statut),
  KEY idx_facture_date_emission (date_emission),
  KEY idx_facture_date_echeance (date_echeance),
  CONSTRAINT fk_facture_client
    FOREIGN KEY (client_id) REFERENCES client(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ligne_facture (
  id BIGINT NOT NULL AUTO_INCREMENT,
  facture_id BIGINT NOT NULL,
  description VARCHAR(255) NOT NULL,
  quantite DECIMAL(19,2) NOT NULL,
  prix_unitaire DECIMAL(19,2) NOT NULL,
  total_ligne DECIMAL(19,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_ligne_facture_facture (facture_id),
  CONSTRAINT fk_ligne_facture_facture
    FOREIGN KEY (facture_id) REFERENCES facture(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS paiement_facture (
  id BIGINT NOT NULL AUTO_INCREMENT,
  facture_id BIGINT NOT NULL,
  montant DECIMAL(19,2) NOT NULL,
  date_paiement DATETIME(6) NOT NULL,
  compte_destination_id BIGINT NOT NULL,
  reference VARCHAR(80) NULL,
  commentaire VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_paiement_facture_facture (facture_id),
  KEY idx_paiement_facture_date (date_paiement),
  KEY idx_paiement_facture_compte (compte_destination_id),
  CONSTRAINT fk_paiement_facture_facture
    FOREIGN KEY (facture_id) REFERENCES facture(id),
  CONSTRAINT fk_paiement_facture_compte
    FOREIGN KEY (compte_destination_id) REFERENCES compte(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS echeance_paiement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  facture_id BIGINT NOT NULL,
  montant_programme DECIMAL(19,2) NOT NULL,
  montant_paye DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  date_prevue DATE NOT NULL,
  statut VARCHAR(15) NOT NULL,
  commentaire VARCHAR(255) NULL,
  date_paiement DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_echeance_facture (facture_id),
  KEY idx_echeance_statut_date (statut, date_prevue),
  CONSTRAINT fk_echeance_facture
    FOREIGN KEY (facture_id) REFERENCES facture(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Dépenses
-- =========================

CREATE TABLE IF NOT EXISTS depense (
  id BIGINT NOT NULL AUTO_INCREMENT,
  titre VARCHAR(160) NOT NULL,
  montant DECIMAL(19,2) NOT NULL,
  categorie VARCHAR(80) NULL,
  date_depense DATE NOT NULL,
  description VARCHAR(255) NULL,
  statut VARCHAR(20) NOT NULL,
  compte_paiement_id BIGINT NULL,
  date_paiement DATETIME(6) NULL,
  date_creation DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_depense_date (date_depense),
  KEY idx_depense_statut (statut),
  KEY idx_depense_compte (compte_paiement_id),
  CONSTRAINT fk_depense_compte
    FOREIGN KEY (compte_paiement_id) REFERENCES compte(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Paie
-- =========================

CREATE TABLE IF NOT EXISTS paie (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employe_id BIGINT NOT NULL,
  mois VARCHAR(7) NOT NULL,
  salaire_base DECIMAL(19,2) NOT NULL,
  primes DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  deductions DECIMAL(19,2) NOT NULL DEFAULT 0.00,
  salaire_net DECIMAL(19,2) NOT NULL,
  statut VARCHAR(15) NOT NULL,
  date_creation DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_paie_employe_mois (employe_id, mois),
  KEY idx_paie_statut (statut),
  KEY idx_paie_mois (mois),
  CONSTRAINT fk_paie_employe
    FOREIGN KEY (employe_id) REFERENCES employe(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS paiement_paie (
  id BIGINT NOT NULL AUTO_INCREMENT,
  paie_id BIGINT NOT NULL,
  montant DECIMAL(19,2) NOT NULL,
  date_paiement DATETIME(6) NOT NULL,
  compte_source_id BIGINT NOT NULL,
  commentaire VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_paiement_paie_paie (paie_id),
  KEY idx_paiement_paie_date (date_paiement),
  KEY idx_paiement_paie_compte (compte_source_id),
  CONSTRAINT fk_paiement_paie_paie
    FOREIGN KEY (paie_id) REFERENCES paie(id),
  CONSTRAINT fk_paiement_paie_compte
    FOREIGN KEY (compte_source_id) REFERENCES compte(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Audit
-- =========================

CREATE TABLE IF NOT EXISTS journal_audit (
  id BIGINT NOT NULL AUTO_INCREMENT,
  utilisateur_id BIGINT NULL,
  action VARCHAR(80) NOT NULL,
  type_entite VARCHAR(60) NULL,
  id_entite BIGINT NULL,
  horodatage DATETIME(6) NOT NULL,
  details VARCHAR(2000) NULL,
  PRIMARY KEY (id),
  KEY idx_audit_horodatage (horodatage),
  KEY idx_audit_action (action),
  KEY idx_audit_type_entite (type_entite),
  KEY idx_audit_utilisateur_id (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Notifications
-- =========================

CREATE TABLE IF NOT EXISTS notification (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(40) NOT NULL,
  statut VARCHAR(20) NOT NULL,
  message VARCHAR(255) NOT NULL,
  type_reference VARCHAR(20) NOT NULL,
  id_reference BIGINT NULL,
  cle_dedup VARCHAR(60) NOT NULL,
  date_creation DATETIME(6) NOT NULL,
  date_lecture DATETIME(6) NULL,
  date_archivage DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_dedup (type, type_reference, id_reference, cle_dedup),
  KEY idx_notification_statut (statut),
  KEY idx_notification_date (date_creation),
  KEY idx_notification_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

