# Documentation Front – API Backend (fof)

## Base URL
- **Dev**: `http://localhost:8080`
- **Prefix API**: `/api`

## Migrations SQL (MySQL, optionnel)
Les scripts sont dans `src/main/resources/db/migration/` :
- `V1__init.sql` — schéma initial
- `V2__ajout_photos.sql` — colonnes `photo_url`
- `V3__entreprise_info.sql` — table **`entreprise_info`** (infos société + logo)
- `V4__facture_statut_brouillon_en_emise.sql` — corrige les factures **`BROUILLON`** avec reste à payer → **`ENVOYEE`** / **`EN_RETARD`** / **`PAYEE`**

> Le changement de **statut facture à la création** (`ENVOYEE` / `EN_RETARD` au lieu de rester en `BROUILLON`) est **uniquement applicatif** : **aucune migration** SQL n’est nécessaire pour ça.

En dev, `spring.flyway.enabled=false` dans `application-dev.properties` : exécute les scripts **à la main** sur ta base si tu es en `ddl-auto=validate` ou sans Hibernate auto-update.

## Authentification (JWT)
### Login
- **POST** `/api/auth/login`

Body:
```json
{
  "email": "admin@gmail.com",
  "motDePasse": "password123"
}
```

Réponse:
```json
{
  "accessToken": "jwt...",
  "refreshToken": "refresh...",
  "expiresInSeconds": 900
}
```

### Header à envoyer
Sur toutes les routes protégées:
- `Authorization: Bearer <accessToken>`

## Images (profils)
Les images sont stockées dans le dossier **static** du serveur et servies directement (ex: `userUpload`, `clientUpload`, `employeUpload`).

Exemple: si `photoUrl="/userUpload/xxx.png"`, le front peut afficher:
- `http://localhost:8080/userUpload/xxx.png`

### Refresh
- **POST** `/api/auth/refresh`

Body:
```json
{ "refreshToken": "refresh..." }
```

### Logout (révoque tous les refresh tokens)
- **POST** `/api/auth/logout-tout`

> Nécessite `Authorization: Bearer ...`

## RBAC (Permissions)
Les permissions sont injectées dans le JWT et exposées côté API via `hasAuthority('PERM_<CODE>')`.

Principales permissions:
- `PERM_FACTURE_CREATE`, `PERM_FACTURE_READ`, `PERM_FACTURE_UPDATE`, `PERM_FACTURE_DELETE`
- `PERM_CLIENT_MANAGE`
- `PERM_EMPLOYEE_MANAGE`
- `PERM_PAYROLL_MANAGE`
- `PERM_EXPENSE_MANAGE`
- `PERM_ACCOUNTING_VIEW`
- `PERM_SETTINGS_MANAGE`

## Entreprise (entête/pied de page factures)
- **GET** `/api/entreprise` (auth requise)
- **PUT** `/api/entreprise` (**PERM_SETTINGS_MANAGE**) — `multipart/form-data` (données + logo optionnel)

### GET `/api/entreprise` (exemple réponse)
```json
{
  "id": 1,
  "nom": "Mon Entreprise",
  "telephone": "770000000",
  "email": "contact@exemple.com",
  "adresse": "Dakar",
  "ville": "Dakar",
  "pays": "SN",
  "ninea": "123456789",
  "rccm": "SN-DKR-2026-A-00001",
  "nomBanque": "CBAO",
  "numeroCompteBanque": "SN12 1234 1234 1234 1234 1234",
  "logoUrl": "/entrepriseUpload/xxx.png"
}
```

### PUT `/api/entreprise` (mise à jour + logo)
`multipart/form-data` avec :
- `donnees` : JSON (`application/json`) de type `ModifierEntrepriseInfoRequest`
- `logo` : fichier image (optionnel)

Exemple JSON (`donnees`) :
```json
{
  "nom": "Mon Entreprise",
  "telephone": "770000000",
  "email": "contact@exemple.com",
  "adresse": "Dakar",
  "ville": "Dakar",
  "pays": "SN",
  "ninea": "123456789",
  "rccm": "SN-DKR-2026-A-00001",
  "nomBanque": "CBAO",
  "numeroCompteBanque": "SN12 1234 1234 1234 1234 1234"
}
```
- `PERM_TREASURY_MANAGE`
- `PERM_SETTINGS_MANAGE`

## Format d’erreur JSON (uniforme)
L’API renvoie (exemples):
```json
{
  "horodatage": "2026-04-15T12:00:00Z",
  "statut": 400,
  "message": "Validation échouée",
  "details": ["email: must be a well-formed email address"]
}
```

Codes:
- **400** validation / règles métiers
- **401** non authentifié
- **403** accès refusé
- **404** ressource introuvable
- **500** erreur interne

## Pagination (Spring Data)
Toutes les listes paginées acceptent:
- `page` (0-based)
- `size`
- `sort` (ex: `sort=id,desc`)

Réponse paginée = format Spring `Page` (`content`, `totalElements`, `totalPages`, etc.).

---

## 1) Trésorerie
### Comptes
- **POST** `/api/comptes` (**PERM_TREASURY_MANAGE**)

```json
{ "nom": "Caisse", "type": "CAISSE" }
```

- **GET** `/api/comptes?recherche=` (**PERM_TREASURY_MANAGE** ou **PERM_ACCOUNTING_VIEW**)

### Transactions
- **POST** `/api/transactions/depot` (**PERM_TREASURY_MANAGE**)

```json
{ "compteDestinationId": 1, "montant": 10000, "description": "Alimentation caisse" }
```

- **POST** `/api/transactions/retrait` (**PERM_TREASURY_MANAGE**)

```json
{ "compteSourceId": 1, "montant": 5000, "description": "Retrait" }
```

- **POST** `/api/transactions/transfert` (**PERM_TREASURY_MANAGE**)

```json
{ "compteSourceId": 1, "compteDestinationId": 2, "montant": 3000, "description": "Transfert" }
```

- **GET** `/api/transactions?type=ENTREE|SORTIE|TRANSFERT` (**PERM_TREASURY_MANAGE** ou **PERM_ACCOUNTING_VIEW**)

### Annulation d’une transaction (écriture inverse)
- **POST** `/api/transactions/{id}/annulation` (**PERM_TREASURY_MANAGE**)

```json
{ "motif": "Erreur de saisie" }
```

---

## 2) Clients
- **POST** `/api/clients` (**PERM_CLIENT_MANAGE**)
- **PUT** `/api/clients/{id}` (**PERM_CLIENT_MANAGE**) — `multipart/form-data` (données + photo optionnelle en **une** requête)
- **GET** `/api/clients/{id}` (**PERM_CLIENT_MANAGE** ou **PERM_ACCOUNTING_VIEW**)
- **GET** `/api/clients/{id}/factures` (**PERM_FACTURE_READ**) — factures du client (hors annulées) + **compteurs** payées / impayées
- **GET** `/api/clients?recherche=` (**PERM_CLIENT_MANAGE** ou **PERM_ACCOUNTING_VIEW**)
- **DELETE** `/api/clients/{id}` (**PERM_CLIENT_MANAGE**)

Body création (**POST**, JSON):
```json
{
  "nomComplet": "Client Exemple",
  "telephone": "770000000",
  "email": "client@mail.com",
  "adresse": "Dakar",
  "pays": "SN",
  "statut": "ACTIF"
}
```

### Mise à jour client + photo (optionnelle)
- **PUT** `/api/clients/{id}` (**PERM_CLIENT_MANAGE**)  
`multipart/form-data` avec :
- `donnees` : **JSON** (type `application/json`) — même structure que ci-dessus (`ModifierClientRequest`)
- `photo` : fichier image (**optionnel**)

### Factures d’un client (liste paginée + compteurs)
- **GET** `/api/clients/{id}/factures?recherche=&etatPaiement=&page=&size=&sort=` (**PERM_FACTURE_READ**)

Query :
- `recherche` (optionnel) : filtre sur le **numéro** de facture
- `etatPaiement` (optionnel) : `TOUTES` (défaut), `PAYEES`, `IMPAYEES` — filtre **uniquement la page** `factures`

Réponse (exemple) :
```json
{
  "compteurs": {
    "nombrePayees": 4,
    "nombreImpayees": 2,
    "nombreTotal": 7
  },
  "factures": [ ],
  "totalElements": 7,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

Définitions :
- **Payée** : `montantRestant = 0`, facture non `ANNULEE`
- **Impayée** (compteur + filtre `IMPAYEES`) : `montantRestant > 0`, hors `ANNULEE` et hors `BROUILLON`
- **nombreTotal** : toutes les factures du client **non annulées** (y compris les brouillons)

> Tu peux aussi utiliser **GET** `/api/factures?clientId={id}` pour une liste simple sans ce bloc `compteurs`.

---

## 3) Factures + Paiements + TVA optionnelle
### Création facture
- **POST** `/api/factures` (**PERM_FACTURE_CREATE**)

```json
{
  "clientId": 1,
  "dateEmission": "2026-04-15",
  "dateEcheance": "2026-04-20",
  "modeTva": "PAR_DEFAUT",
  "tauxTva": 0.18,
  "lignes": [
    { "description": "Billet", "quantite": 1, "prixUnitaire": 50000 }
  ]
}
```

`modeTva`:
- `DESACTIVEE` → TVA=0
- `PAR_DEFAUT` → TVA = `app.tva.taux-par-defaut` si `app.tva.activee=true`
- `PERSONNALISEE` → TVA = `tauxTva`

> **Statut à la création** : `ENVOYEE` (facture émise, reste à payer), ou `EN_RETARD` si `dateEcheance` est **déjà dépassée**, ou `PAYEE` si le total TTC est **0**.

### Liste / détail
- **GET** `/api/factures?recherche=&clientId=&statut=` (**PERM_FACTURE_READ**)
- **GET** `/api/clients/{id}/factures` (**PERM_FACTURE_READ**) — même périmètre client, avec **compteurs** payées / impayées (voir section Clients)
- **GET** `/api/factures/{id}` (**PERM_FACTURE_READ**)

### Paiement facture (partiel/total)
- **POST** `/api/factures/{id}/paiements` (**PERM_FACTURE_UPDATE** ou **PERM_TREASURY_MANAGE**)

```json
{
  "compteDestinationId": 1,
  "montant": 10000,
  "reference": "OM-123",
  "commentaire": "Acompte"
}
```

> Le paiement crée automatiquement une transaction trésorerie (ENTREE) avec `typeReference=FACTURE`.

### Création facture + paiement (en une requête)
- **POST** `/api/factures/avec-paiement` (**PERM_FACTURE_CREATE** + (`PERM_FACTURE_UPDATE` ou `PERM_TREASURY_MANAGE`))

```json
{
  "clientId": 1,
  "dateEmission": "2026-04-16",
  "dateEcheance": "2026-04-30",
  "modeTva": "DESACTIVEE",
  "lignes": [
    { "description": "Prestation", "quantite": 1, "prixUnitaire": 50000 }
  ],
  "paiement": {
    "compteDestinationId": 1,
    "montant": 20000,
    "reference": "OM-123",
    "commentaire": "Acompte"
  }
}
```

---

## 4) Échéances (Planification paiement facture)
- **POST** `/api/factures/{factureId}/echeances/plan` (**PERM_FACTURE_UPDATE**)

```json
{
  "echeances": [
    { "montantProgramme": 10000, "datePrevue": "2026-04-16", "commentaire": "Acompte" },
    { "montantProgramme": 40000, "datePrevue": "2026-04-20", "commentaire": "Solde" }
  ]
}
```

- **GET** `/api/factures/{factureId}/echeances` (**PERM_FACTURE_READ**)

> Quand une facture est payée, le backend répartit automatiquement le paiement sur les échéances `EN_ATTENTE`.

---

## 5) Dépenses
- **POST** `/api/depenses` (**PERM_EXPENSE_MANAGE**)
- **PUT** `/api/depenses/{id}` (**PERM_EXPENSE_MANAGE**)
- **GET** `/api/depenses/{id}` (**PERM_EXPENSE_MANAGE** ou **PERM_ACCOUNTING_VIEW**)
- **GET** `/api/depenses?recherche=&statut=&debut=YYYY-MM-DD&fin=YYYY-MM-DD` (**PERM_EXPENSE_MANAGE** ou **PERM_ACCOUNTING_VIEW**)
- **POST** `/api/depenses/{id}/approbation` (**PERM_EXPENSE_MANAGE**)
- **POST** `/api/depenses/{id}/paiement` (**PERM_EXPENSE_MANAGE** ou **PERM_TREASURY_MANAGE**)
- **DELETE** `/api/depenses/{id}` (**PERM_EXPENSE_MANAGE**)

Payer:
```json
{ "comptePaiementId": 1 }
```

> Le paiement crée automatiquement une transaction trésorerie (SORTIE) avec `typeReference=DEPENSE`.

---

## 6) Employés
- **POST** `/api/employes` (**PERM_EMPLOYEE_MANAGE**)
- **PUT** `/api/employes/{id}` (**PERM_EMPLOYEE_MANAGE**)
- **GET** `/api/employes/{id}` (**PERM_EMPLOYEE_MANAGE** ou **PERM_PAYROLL_MANAGE**)
- **GET** `/api/employes?recherche=` (**PERM_EMPLOYEE_MANAGE** ou **PERM_PAYROLL_MANAGE**)
- **DELETE** `/api/employes/{id}` (**PERM_EMPLOYEE_MANAGE**)

### Créer un employé
Body:
```json
{
  "nom": "DIOP",
  "prenom": "Awa",
  "telephone": "770000001",
  "fonction": "Agent",
  "salaireBase": 150000,
  "typeContrat": "CDI",
  "dateEmbauche": "2026-04-15",
  "statut": "ACTIF"
}
```

`statut`:
- `ACTIF`
- `INACTIF`

### Photo employé
- **PUT** `/api/employes/{id}/photo` (**PERM_EMPLOYEE_MANAGE**)  
`multipart/form-data` avec champ fichier `photo`.

---

## 6.b) Profil utilisateur (self)
- **GET** `/api/profil`
- **PUT** `/api/profil` (multipart/form-data)

Pour la mise à jour (un seul endpoint), envoyer:
- `donnees` = JSON (`ModifierProfilRequest`)
- `photo` = fichier image (optionnel)

Exemple `donnees`:
```json
{
  "nom": "Admin",
  "prenom": "Principal",
  "telephone": "770000002",
  "email": "admin@gmail.com"
}
```

## 7) Paie
- **POST** `/api/paies` (**PERM_PAYROLL_MANAGE**)
- **POST** `/api/paies/{id}/approbation` (**PERM_PAYROLL_MANAGE**)
- **POST** `/api/paies/{id}/paiement` (**PERM_PAYROLL_MANAGE** ou **PERM_TREASURY_MANAGE**)
- **GET** `/api/paies/{id}` (**PERM_PAYROLL_MANAGE**)
- **GET** `/api/paies?employeId=&statut=&mois=YYYY-MM` (**PERM_PAYROLL_MANAGE**)
- **DELETE** `/api/paies/{id}` (**PERM_PAYROLL_MANAGE**)

Payer:
```json
{ "compteSourceId": 1, "montant": 150000, "commentaire": "Virement" }
```

> Le paiement crée automatiquement une transaction trésorerie (SORTIE) avec `typeReference=PAIE`.

---

## 8) Audit
- **GET** `/api/audits?recherche=&utilisateurId=&action=&typeEntite=&debut=&fin=`  
  (**PERM_ACCOUNTING_VIEW** ou **PERM_SETTINGS_MANAGE**)

---

## 9) Notifications
- **GET** `/api/notifications?recherche=&type=&statut=&debut=&fin=`  
  (**PERM_ACCOUNTING_VIEW** ou **PERM_SETTINGS_MANAGE** ou **PERM_FACTURE_READ**)
- **POST** `/api/notifications/{id}/lue` (mêmes droits que le GET)
- **POST** `/api/notifications/{id}/archiver` (mêmes droits que le GET)

> Les alertes facture (échéance proche / retard) sont créées par le job `app.alertes.cron` (souvent toutes les 10 min).  
> Si tu ne vois rien : vérifie le **statut** de la facture (les alertes incluent les **`BROUILLON`** dès qu’une **`dateEcheance`** est renseignée, même sans reste à payer ; pour les autres statuts il faut un **montant restant** strictement positif), le filtre **`statut`** côté liste notifications, et que tu as bien un droit de lecture ci-dessus.

---

## 10) Dashboard
- **GET** `/api/dashboard?annee=2026&tailleOperationsRecentes=10` (**PERM_ACCOUNTING_VIEW**)

Config : `app.dashboard.echeance-prochaine-jours` (défaut **3**) — fenêtre **[aujourd’hui, aujourd’hui + N]** pour les compteurs « échéances proches ».

Retourne aussi:
- `devise`
- séries 12 mois : **`chiffreAffairesParMois`** = **CA encaissé** par mois (somme des **paiements**, date du paiement) ; **`depensesParMois`** ; **`fluxParMois`**
- **`chiffreAffairesTotal`** : **CA encaissé cumulé** (somme des `montantPaye` sur factures non annulées). Une facture **sans paiement** n’y contribue **pas**.
- **`chiffreAffairesEncaisseDuMois`** : encaissements (**paiements**) sur le **mois civil en cours** (UTC, aligné trésorerie).
- **`chiffreAffairesFactureDuMois`** : total **TTC facturé** (`dateEmission` dans le mois civil en cours, hors `ANNULEE`).
- **`echeanceProchaineFenetreJours`** : valeur **N** utilisée pour les champs suivants.
- **`nombreFacturesEcheanceProche`** / **`montantDuFacturesEcheanceProche`** : factures avec **reste à payer** et **`dateEcheance`** dans la fenêtre (hors `ANNULEE`, `PAYEE`).
- **`nombreEcheancesPlanEnAttenteProche`** / **`montantRestantProgrammeEcheancesPlanProches`** : lignes **`echeance_paiement`** `EN_ATTENTE` dont **`datePrevue`** est dans la fenêtre, facture non `ANNULEE` ; montant = somme **`montantProgramme - montantPaye`**.
- `operationsRecentes`
- `facturesParStatut`

---

## 11) Admin Sécurité (RBAC)
Tous les endpoints ci-dessous nécessitent **`PERM_SETTINGS_MANAGE`**.

- **GET** `/api/admin/securite/permissions?recherche=`
- **POST** `/api/admin/securite/roles`
- **GET** `/api/admin/securite/roles?recherche=`
- **PUT** `/api/admin/securite/roles/{roleId}/permissions`
- **DELETE** `/api/admin/securite/roles/{roleId}`
- **POST** `/api/admin/securite/utilisateurs`
- **PUT** `/api/admin/securite/utilisateurs/{id}`
- **GET** `/api/admin/securite/utilisateurs?recherche=`
- **PUT** `/api/admin/securite/utilisateurs/{id}/roles`
- **PUT** `/api/admin/securite/utilisateurs/{id}/mot-de-passe`
- **DELETE** `/api/admin/securite/utilisateurs/{id}`

