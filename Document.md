📘 CAHIER DES CHARGES – APPLICATION DE GESTION D’ENTREPRISE

(Agence de voyage / prestation de services)

1. 🎯 Objectif du système

Développer une application de gestion intégrée permettant à une entreprise de :

Gérer la facturation clients (paiement partiel ou total)
Planifier et suivre les paiements clients
Gérer les employés et leurs salaires
Gérer les dépenses et entrées/sorties financières
Assurer une comptabilité simple mais robuste (trésorerie)
Suivre les soldes multi-comptes (Banque, Orange Money, Caisse)
Contrôler les accès utilisateurs par permissions fines (RBAC)
Configurer la TVA selon les cas de factures
2. 🧩 MODULES FONCTIONNELS
2.1 👥 Gestion des utilisateurs & permissions (RBAC)
2.1.1 Utilisateurs

Un utilisateur représente un employé ou administrateur.

Champs :
id
nom
prénom
téléphone
email
mot de passe (hashé)
statut (ACTIF / INACTIF)
rôle principal
2.1.2 Système de rôles & permissions

Le système doit être basé sur un modèle RBAC (Role-Based Access Control).

Entités :
Role
Permission
UserRole
RolePermission
Permissions possibles :
FACTURE_CREATE
FACTURE_READ
FACTURE_UPDATE
FACTURE_DELETE
CLIENT_MANAGE
EMPLOYEE_MANAGE
PAYROLL_MANAGE
EXPENSE_MANAGE
ACCOUNTING_VIEW
TREASURY_MANAGE
SETTINGS_MANAGE
Règles :
Le propriétaire (admin principal) a tous les droits
Les rôles sont configurables dynamiquement
Un utilisateur peut avoir plusieurs permissions via rôle
2.2 👤 Gestion des clients
Fonctionnalités :
Ajouter / modifier / supprimer client
Historique complet des factures
Solde client (dettes / avances)
Champs Client :
id
nom complet
téléphone
email
adresse
pays
date création
statut
2.3 🧾 Gestion des factures
Fonctionnalités :
Création de facture
Facture avec TVA optionnelle
Paiement partiel ou total
Historique des paiements
Statut automatique
Statuts facture :
DRAFT
SENT
PARTIALLY_PAID
PAID
OVERDUE
CANCELLED
Structure Facture :
Invoice
id
client_id
date_emission
date_echeance
total_ht
tva_percentage (nullable)
total_ttc
montant_paye
montant_restant
statut
InvoiceLine
id
invoice_id
description
quantité
prix_unitaire
total_ligne
Paiements facture :
Payment
id
invoice_id
montant
date_paiement
méthode (CASH / BANK / ORANGE_MONEY)
référence
commentaire
🔔 Planification des paiements (IMPORTANT)

Chaque facture peut avoir un plan de paiement.

PaymentSchedule :
id
invoice_id
montant_programmé
date_prevue
statut (PENDING / PAID / MISSED)
commentaire

👉 Le système doit envoyer des alertes :

paiement imminent
paiement en retard
2.4 💰 Gestion des employés & paie
Employé :
id
nom
prénom
téléphone
fonction
salaire_base
type_contrat
date_embauche
statut
Paie :
Payroll
id
employee_id
mois
salaire_base
primes
déductions
salaire_net
statut (UNPAID / PAID)
PayrollPayment
id
payroll_id
montant
date
méthode
2.5 💸 Gestion des dépenses
Expense :
id
titre
montant
catégorie (carburant, logistique, etc.)
date
méthode de paiement
description
créé_par
2.6 🏦 Trésorerie / Comptabilité
Objectif :

Suivre tous les flux financiers entrants et sortants

Accounts (Comptes financiers)
Account :
id
nom (Banque / Orange Money / Caisse)
type (BANK / MOBILE_MONEY / CASH)
solde_actuel
Transactions (TRÈS IMPORTANT)

Toute opération doit créer une transaction automatique :

Transaction :
id
type (INCOME / EXPENSE / TRANSFER)
montant
date
source_account
destination_account
reference_type (INVOICE / EXPENSE / PAYROLL / MANUAL)
reference_id
description
🔁 Règles comptables :
Paiement facture = INCOME
Dépense = EXPENSE
Paiement salaire = EXPENSE
Transfert entre comptes = TRANSFER
2.7 💳 Gestion des comptes financiers

L’entreprise peut avoir :

Banque
Orange Money
Caisse physique
Fonctionnalités :
Voir solde en temps réel
Alimenter un compte (deposit)
Retirer de l’argent
Transférer entre comptes
2.8 ⚙️ Paramètres système
TVA :
Activable / désactivable
Taux configurable globalement
Possibilité de désactiver par facture
Autres paramètres :
Devise
Format date
règles de numérotation facture
2.9 📊 Tableau de bord (Dashboard)

Indicateurs :

Chiffre d’affaires total
Dettes clients
Paiements en attente
Dépenses du mois
Salaires à payer
Solde global entreprise
Solde par compte
2.10 🔔 Système d’alertes

Notifications automatiques :

Facture proche échéance
Facture en retard
Salaire à payer
Faible solde caisse/compte
Paiement planifié imminent
3. 🧱 ARCHITECTURE BACKEND (SPRING BOOT)
Architecture recommandée :
Controller Layer (REST API)
Service Layer (logique métier)
Repository Layer (JPA/Hibernate)
DTO Layer (sécurité API)
Security Layer (Spring Security + JWT)
Modules Spring Boot :
auth-service
user-service
client-service
invoice-service
payment-service
payroll-service
expense-service
accounting-service
treasury-service
notification-service
4. 🔐 SÉCURITÉ
Authentification JWT
Refresh token
Hash password BCrypt
RBAC strict
Audit log sur actions sensibles
5. 🧾 AUDIT & TRAÇABILITÉ

Chaque action critique doit être tracée :

création facture
paiement
suppression
modification montant
transfert argent
AuditLog :
user_id
action
entity_type
entity_id
timestamp
details
6. 📌 POINTS CRITIQUES (IMPORTANT POUR TON DEV)

✔ Tout mouvement d’argent doit créer une Transaction
✔ Une facture doit pouvoir être partiellement payée
✔ Le système doit toujours connaître le solde exact des comptes
✔ La comptabilité doit être dérivable uniquement via Transactions
✔ Pas de manipulation directe des soldes sans transaction
✔ Permissions doivent être dynamiques et strictes
✔ Planification de paiement obligatoire pour suivi client

7. 🚀 EXTENSIONS FUTURES (OPTIONNEL)
Export PDF factures
WhatsApp/SMS alertes clients
Multi-entreprise
API mobile
Synchronisation bancaire
Dashboard analytics avancé



--------
# NB
- Les API du projet doit etre fait en Springboot
- tu doit bien configurer : pom.xml
   avec tout ce qui va avec comme lambok et tout par une une application clean et optimal

# Performance
- tu doit faire pagination partout ou ya la liste et donne la possibiliter de search dune façons
      vraiment optimal sa couter le CPU ou Ram.
- tu doit vraimenet rendre scallable
- les requette doit etre optimal sans boucle
- les DTO doit etre bien fait
- la securite doit etre solide

# Code clean
- pas besoin de commenter chaque ligne juste des grands ligne