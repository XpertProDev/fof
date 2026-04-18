-- Infos entreprise (singleton id = 1) pour entête / pied de page factures
-- Exécuter ce script sur une base existante si tu n’utilises pas JPA `ddl-auto=update`.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS entreprise_info (
  id BIGINT NOT NULL,
  nom VARCHAR(160) NOT NULL,
  telephone VARCHAR(120) NULL,
  email VARCHAR(120) NULL,
  adresse VARCHAR(255) NULL,
  ville VARCHAR(80) NULL,
  pays VARCHAR(80) NULL,
  ninea VARCHAR(80) NULL,
  rccm VARCHAR(80) NULL,
  nom_banque VARCHAR(120) NULL,
  numero_compte_banque VARCHAR(120) NULL,
  logo_url VARCHAR(255) NULL,
  pied_de_page VARCHAR(1000) NULL,
  date_mise_ajour DATETIME(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
