-- Ajout des colonnes photo_url

ALTER TABLE utilisateur ADD COLUMN photo_url VARCHAR(255) NULL;
ALTER TABLE client ADD COLUMN photo_url VARCHAR(255) NULL;
ALTER TABLE employe ADD COLUMN photo_url VARCHAR(255) NULL;

