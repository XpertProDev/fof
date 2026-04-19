# Comptabilité / trésorerie — guide d’intégration front

Ce document décrit le **contrat API** pour la page **`/comptabilite`** (remplacement des données statiques). Le backend correspondant est **implémenté** dans ce projet (`CompteController`, `TransactionTresorerieController`, `TransactionTresorerieService`).

Pour le détail auth, erreurs et pagination Spring, voir aussi **`FRONT_DOCUMENTATION.md`** (§ 1 Trésorerie).

---

## 1. Permissions

| Permission | Usage |
|------------|--------|
| `PERM_TREASURY_MANAGE` | Création de comptes, dépôt / retrait / transfert, annulation de transaction |
| `PERM_ACCOUNTING_VIEW` | Lecture seule : liste comptes, journal, rapport mensuel |

Toutes les routes ci-dessous exigent un JWT valide : `Authorization: Bearer <accessToken>`.

---

## 2. Comptes (`CompteVm`)

### Champs JSON (`GET` / `POST` réponse)

| Champ | Type | Description |
|--------|------|-------------|
| `id` | number | Identifiant |
| `nom` | string | 2–120 caractères |
| `type` | string | `CAISSE`, `BANQUE`, `ORANGE_MONEY`, `AUTRE` |
| `solde` | number | Solde courant (alias métier du `solde_actuel` en base) |
| `devise` | string | Ex. `XOF` — valeur `app.devise` côté serveur |

### Routes

| Méthode | URL | Permission | Corps / query |
|---------|-----|------------|-----------------|
| **POST** | `/api/comptes` | `PERM_TREASURY_MANAGE` | `{ "nom": "…", "type": "CAISSE" }` → **201 Created** |
| **GET** | `/api/comptes` | `PERM_TREASURY_MANAGE` **ou** `PERM_ACCOUNTING_VIEW` | `recherche=` (optionnel, nom insensible à la casse), pagination Spring `page`, `size`, `sort` |

Réponse liste : page Spring (`content`, `totalElements`, …), chaque élément = objet §2.

---

## 3. Mouvements / journal (`MouvementTresorerieVm`)

### Champs JSON (`GET /api/transactions`)

| Champ | Type | Notes |
|--------|------|--------|
| `id` | number | |
| `date` | string ISO-8601 | Instant UTC (ex. `2026-04-17T16:31:00Z`) |
| `type` | string | `ENTREE`, `SORTIE`, `TRANSFERT` |
| `libelle` | string | Description métier (ou libellé dérivé si vide : facture / dépense / paie) |
| `montant` | number | Toujours **> 0** ; le sens vient de `type` |
| `devise` | string | Aligné `app.devise` |
| `compteNom` | string \| null | **ENTREE** / **SORTIE** : nom du compte impacté (colonne « Compte / flux ») |
| `compteSource` | string \| null | **TRANSFERT** : compte débité |
| `compteDestination` | string \| null | **TRANSFERT** : compte crédité |
| `reference` | string \| null | Ex. `FACTURE:42`, `DEPENSE:3` ; `null` si opération manuelle sans référence |
| `statut` | string | Aujourd’hui `POSTEE` sur tout ce qui est listé (voir §5) |

### Routes journal

| Méthode | URL | Permission | Query |
|---------|-----|------------|--------|
| **GET** | `/api/transactions` | `PERM_TREASURY_MANAGE` **ou** `PERM_ACCOUNTING_VIEW` | `type` (optionnel) : `ENTREE` \| `SORTIE` \| `TRANSFERT` ; **`recherche=`** (libellé, noms de comptes) ; **`debut=` / `fin=`** (date ISO `YYYY-MM-DD`, bornes **jour civil UTC**) ; pagination Spring |

- Tri par défaut si `sort` absent : **`date` décroissante** (`dateOperation`).
- Les écritures liées aux **paiements facture**, **dépenses**, **paie** apparaissent avec le bon `type` et une `reference` exploitable.

### Dépôt / retrait / transfert / annulation

| Méthode | URL | Corps (JSON) |
|---------|-----|----------------|
| **POST** | `/api/transactions/depot` | `{ "compteDestinationId", "montant", "description" }` |
| **POST** | `/api/transactions/retrait` | `{ "compteSourceId", "montant", "description" }` |
| **POST** | `/api/transactions/transfert` | `{ "compteSourceId", "compteDestinationId", "montant", "description" }` |
| **POST** | `/api/transactions/{id}/annulation` | `{ "motif": "…" }` |

Toutes ces **POST** (sauf annulation) : `PERM_TREASURY_MANAGE`.

**Annulation** : une écriture **inverse** est créée (statut `POSTEE`) ; la transaction d’origine passe en **`ANNULEE`** et **n’apparaît plus** dans `GET /api/transactions` (journal = mouvements postés non annulés).

---

## 4. Reporting mensuel

**GET** `/api/transactions/rapport-mensuel?debut=YYYY-MM&fin=YYYY-MM`  
Permission : `PERM_TREASURY_MANAGE` **ou** `PERM_ACCOUNTING_VIEW`.

- `debut` / `fin` : **inclus**, mois calendaires (`YearMonth`).
- Réponse : **tableau** trié chronologiquement, une entrée par mois même sans mouvement :

```json
[
  { "mois": "2025-11", "encaissements": 0, "decaissements": 0 },
  { "mois": "2025-12", "encaissements": 150000, "decaissements": 40000 }
]
```

Règles agrégat (aligné bilan « cash in / out ») :

- `encaissements` = somme des **ENTREE** `POSTEE` sur le mois (date d’opération).
- `decaissements` = somme des **SORTIE** `POSTEE`.
- Les **TRANSFERT** ne comptent ni dans l’un ni dans l’autre.
- Les transactions **ANNULEE** sont exclues.

---

## 5. Synthèse front (écrans)

| Vue | Source API |
|-----|------------|
| Liste comptes + % | `GET /api/comptes` (`solde` + `devise`) |
| Tableau mouvements | `GET /api/transactions` + filtres |
| Reporting barres / courbes | `GET /api/transactions/rapport-mensuel` |
| KPI « bilan » (optionnel client) | Dériver de comptes + journal, ou endpoint dédié plus tard |

---

## 6. Validations utiles (UX)

- Nom compte : **2–120** caractères ; type hors enum → **400** (erreur structurée habituelle).
- Montants dépôt / retrait / transfert : **> 0** ; retrait / transfert : solde suffisant.
- Rapport : `debut` ≤ `fin` ; format `YYYY-MM` invalide → **400**.

---

## 7. Historique document

L’ancienne version de ce fichier était un **prompt** pour l’équipe backend. Les points §3–§4 sont **livré** dans le code actuel ; toute évolution (ex. `inclureAnnulees=true`, endpoint bilan agrégé) sera reflétée ici et dans **`FRONT_DOCUMENTATION.md`**.
