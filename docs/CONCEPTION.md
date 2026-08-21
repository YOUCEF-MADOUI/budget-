# Budget++ — Dossier de conception fonctionnelle et technique

> Statut : conception validable avant développement  
> Portée : Android natif, version 1.0 hors ligne  
> Langue de référence : français  
> Principe directeur : **simple à saisir, fiable à calculer, privé par défaut**

## Décisions structurantes

1. Les montants sont stockés en `Long`, en unité monétaire mineure (`580000` = `5 800,00 DZD`). Aucun `Float` ou `Double` n'est utilisé pour un calcul financier.
2. Room/SQLite est la source de vérité. L'interface ne dépend jamais directement des DAO.
3. Une transaction de transfert est un objet métier unique, appliqué atomiquement au compte source et au compte destination. Elle est exclue des revenus et dépenses.
4. Budget++ fonctionne sans compte, réseau ou service distant. Toute sortie de données exige une action explicite.
5. L'architecture sépare domaine, données et présentation pour permettre une future synchronisation sans réécrire le métier.
6. La V1 privilégie un parcours de saisie inférieur à dix secondes et reporte les fonctions avancées qui menaceraient sa fiabilité.

---

# A. Architecture fonctionnelle complète

## A.1 Domaines fonctionnels

| Domaine | Responsabilité | V1 |
|---|---|---:|
| Démarrage | Splash, onboarding, configuration initiale | Oui |
| Espaces | Séparation Personnel/Entreprise/Famille | Préparé, un espace actif en V1 |
| Comptes | Création, édition, archivage, soldes | Oui |
| Opérations | Revenus, dépenses, transferts, historique | Oui |
| Catégorisation | Catégories, sous-catégories, couleurs, icônes | Oui |
| Budgets | Budget global et par catégorie, progression, alertes | Oui |
| Récurrences | Règles, prochaines occurrences, génération fiable | Oui |
| Dashboard | Synthèse, comptes, budget, opérations récentes | Oui |
| Analyse | Indicateurs et graphiques essentiels | Oui |
| Recherche | Recherche textuelle et filtres combinables | Oui |
| Portabilité | Export CSV, sauvegarde/restauration chiffrée | Oui |
| Sécurité | PIN, biométrie, verrouillage automatique | Oui |
| Préférences | Devise, thème, langue prête, début de mois | Oui |
| Objectifs | Objectifs d'épargne | V2 |
| Échéancier/calendrier | Paiements à venir et vue calendrier | V2 |
| Imports/rapports | CSV bancaire, XLSX, PDF | V2 |
| Prévisions | Trésorerie future | V2 |
| Cloud/partage/assistant | Synchronisation et usages collaboratifs | V3 |

## A.2 Parcours critiques

### Première utilisation

`Splash → Bienvenue → 4 pages d'onboarding → Profil facultatif → Devise → Premier compte → Solde initial → Début du mois → Dashboard`

L'onboarding n'est marqué terminé qu'après la création réussie du premier compte. Une interruption reprend à la dernière étape sûre.

### Ajout rapide

`Bouton + → Dépense/Revenu/Transfert → Montant → Catégorie → Compte → Enregistrer`

- Le montant reçoit le focus automatiquement.
- Les derniers compte et catégorie compatibles sont proposés.
- Date/heure = maintenant.
- Les champs secondaires sont repliés sous « Plus de détails ».
- L'enregistrement est désactivé tant que les trois champs requis sont invalides.

### Consultation et correction

`Historique → Recherche/filtres → Détail → Modifier ou Supprimer → Confirmation → Totaux recalculés`

### Budget mensuel

`Budgets → Ajouter → Global ou catégorie → Période/montant/seuils → Enregistrer → Progression et alerte locale`

### Récurrence

`Opération → Rendre récurrente → Fréquence/date de début/fin → Aperçu → Enregistrer`

WorkManager prépare les occurrences à échéance, mais la même logique s'exécute aussi à l'ouverture de l'application : le fonctionnement ne dépend donc pas de la ponctualité du système.

## A.3 Règles métier principales

- **Solde d'un compte** = solde initial + revenus − dépenses + transferts entrants − transferts sortants, hors opérations supprimées.
- **Solde global** = somme des comptes actifs et visibles dans la devise de référence. La conversion multidevise n'étant pas en V1, les comptes d'une autre devise sont regroupés séparément et jamais additionnés artificiellement.
- **Disponible du mois** = revenus du mois − dépenses du mois.
- **Dépensé d'un budget** = somme des dépenses éligibles dans sa période, hors transferts et opérations supprimées.
- Une catégorie de budget vide signifie « budget global ».
- Les périodes suivent le fuseau local enregistré et le jour de début du mois budgétaire.
- La suppression métier est logique (`deletedAt`) pour les comptes et opérations. Une purge définitive est séparée et confirmée.
- Un compte référencé ne peut pas être supprimé physiquement ; il peut être archivé.
- Une catégorie utilisée peut être archivée ou remplacée, sans casser l'historique.
- Une occurrence récurrente possède une clé unique `(recurringId, dueDate)` afin d'éviter les doublons.
- Les alertes à 50/75/90/100 % ne sont émises qu'une fois par budget et par période/seuil.

## A.4 États transversaux

Chaque écran distingue :

- chargement initial ;
- contenu ;
- contenu vide avec action utile ;
- erreur récupérable avec message utilisateur ;
- erreur bloquante avec option de réessai ;
- action en cours et succès transitoire.

Les erreurs techniques sont journalisées en développement, sans montant, description ou autre donnée financière dans les logs de production.

---

# B. Architecture technique Android

## B.1 Socle

- Kotlin ; Jetpack Compose ; Material 3
- Navigation Compose avec routes typées
- ViewModel, `StateFlow`, coroutines
- Room/SQLite avec migrations explicites
- DataStore Preferences pour préférences non relationnelles
- WorkManager pour récurrences et maintenance
- Hilt pour l'injection
- Paging 3 pour les grands historiques
- AndroidX Security/Keystore et Biometric
- JUnit, kotlinx-coroutines-test, Turbine, Room Test, Compose UI Test

Les versions exactes et le `compileSdk` seront verrouillés dans le catalogue Gradle en phase 2 après vérification de la version stable et des exigences Google Play du moment.

## B.2 Clean Architecture pragmatique

```text
Compose Screen
    ↓ événements / UiState
ViewModel
    ↓ use cases
Domain (modèles, règles, interfaces de repository)
    ↓ implémentations
Data (Room, DataStore, fichiers, workers)
    ↓
SQLite / Keystore / stockage choisi par l'utilisateur
```

### Dépendances autorisées

- `presentation` dépend de `domain` et de `core`.
- `data` dépend de `domain`, de `database` et de `core`.
- `domain` ne dépend d'aucun framework Android.
- `database` ne connaît pas Compose.
- `app` assemble les modules et la navigation.

Aucun type `Entity`, `Cursor`, `Context` ou DTO ne traverse vers le domaine.

## B.3 Stratégie de modularisation

La V1 démarre avec des modules stables par responsabilité, et non un module par petit écran. Les fonctionnalités volumineuses sont isolées. Ceci maintient des temps de build raisonnables sans créer un monolithe.

- `:app` : application, graphe de navigation racine, DI finale.
- `:core:*` : types communs, design system, utilitaires Android, tests.
- `:domain` : métier Kotlin pur.
- `:data` : repositories et sources locales.
- `:database` : Room, DAO, migrations.
- `:feature:*` : présentation par domaine.

## B.4 Gestion d'état

Chaque ViewModel expose un unique `StateFlow<UiState>` immuable et reçoit des événements explicites. Les effets ponctuels (navigation, snackbar) utilisent un flux d'effets séparé ou sont consommés par identifiant. Les données Room sont observées en `Flow`; les écritures passent par des use cases transactionnels.

## B.5 Argent, dates et devises

```kotlin
data class Money(val minorUnits: Long, val currencyCode: String)
```

- Addition/soustraction interdites entre devises différentes.
- `Math.addExact`/`subtractExact` ou fonctions équivalentes détectent un dépassement.
- Formatage uniquement en présentation via `NumberFormat` et métadonnées de devise.
- `Instant` pour les instants techniques ; date/heure locale + zone pour les échéances civiles.
- `LocalDate` pour période budgétaire et date d'occurrence.
- Convertisseurs Room centralisés et testés.

## B.6 Transactions et concurrence

- Les créations/modifications de transfert utilisent `RoomDatabase.withTransaction`.
- Les agrégats sont calculés par requêtes SQL indexées, pas en chargeant tout l'historique.
- Les écritures récurrentes sont idempotentes grâce à un index unique.
- `updatedAt` permettra une synchronisation future ; `syncState` ne sera ajouté que lorsqu'un protocole de synchronisation existera.

## B.7 Sécurité et confidentialité

- Aucun permission Internet dans la V1, sauf nécessité explicite ultérieurement documentée.
- Sauvegarde Android automatique désactivée pour la base financière ; export volontaire via Storage Access Framework.
- PIN : dérivation lente et salée, vérificateur protégé par une clé Android Keystore ; jamais de PIN en clair.
- Biométrie : `BiometricPrompt`, avec clés invalidées selon la politique choisie.
- Sauvegarde : archive versionnée, chiffrée avec AES-GCM ; clé dérivée d'une phrase secrète via KDF adaptée et sel aléatoire.
- Fichiers temporaires effacés au mieux après partage/export.
- Captures d'écran masquables sur écrans sensibles via paramètre (`FLAG_SECURE`).
- Logs de production sans données financières ni secrets.
- La restauration vérifie version, intégrité, authentification et espace disponible avant remplacement atomique.

## B.8 Performance

- Index sur dates, comptes, catégories, type, suppression et colonnes de recherche utiles.
- Paging 3 et `LazyColumn` avec clés stables.
- Projections SQL dédiées au dashboard et aux statistiques.
- Recherche FTS5 envisagée après benchmark ; fallback Room `LIKE` correctement échappé.
- Mises à jour ciblées, modèles Compose stables, graphiques limités à des séries agrégées.
- Jeux de tests à 10 k, 50 k et 100 k opérations.

## B.9 Extensibilité cloud

Les identifiants UUID, horodatages, repositories abstraits et limites de couche préparent une source distante. Ils ne prétendent pas résoudre les conflits. Une future synchronisation exigera journal de mutations, tombstones, chiffrement de bout en bout et stratégie de résolution documentée.

---

# C. Arborescence du projet

```text
budget-/
├── app/
│   └── src/main/kotlin/com/budgetplus/app/
│       ├── BudgetPlusApplication.kt
│       ├── MainActivity.kt
│       ├── di/
│       └── navigation/
├── build-logic/
│   └── convention/                 # plugins Gradle conventionnels
├── core/
│   ├── common/                     # Result, dispatcher, horloge, erreurs
│   ├── model/                      # modèles UI transversaux uniquement
│   ├── designsystem/               # thème, composants, icônes originales
│   ├── ui/                         # états vides/erreurs, formatage UI
│   ├── security/                   # Keystore, verrouillage, chiffrement
│   └── testing/                    # fixtures, fakes, règles de test
├── domain/
│   └── src/main/kotlin/com/budgetplus/domain/
│       ├── model/
│       ├── repository/
│       ├── usecase/
│       │   ├── account/
│       │   ├── transaction/
│       │   ├── budget/
│       │   ├── recurring/
│       │   ├── statistics/
│       │   └── backup/
│       └── validation/
├── database/
│   ├── schemas/                    # schémas Room versionnés
│   └── src/main/kotlin/com/budgetplus/database/
│       ├── BudgetPlusDatabase.kt
│       ├── dao/
│       ├── entity/
│       ├── relation/
│       ├── migration/
│       └── converter/
├── data/
│   └── src/main/kotlin/com/budgetplus/data/
│       ├── repository/
│       ├── mapper/
│       ├── datastore/
│       ├── backup/
│       ├── export/
│       └── worker/
├── feature/
│   ├── onboarding/
│   ├── dashboard/
│   ├── accounts/
│   ├── transactions/
│   ├── categories/
│   ├── budgets/
│   ├── recurring/
│   ├── analytics/
│   ├── search/
│   ├── backup/
│   ├── settings/
│   └── security/
├── docs/
│   ├── CONCEPTION.md
│   ├── ADR/                         # décisions d'architecture
│   ├── PRIVACY.md
│   └── TESTING.md
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

Structure interne d'une feature :

```text
feature/accounts/src/main/kotlin/.../accounts/
├── navigation/AccountsNavigation.kt
├── list/AccountsScreen.kt
├── list/AccountsViewModel.kt
├── detail/AccountDetailScreen.kt
├── detail/AccountDetailViewModel.kt
├── edit/EditAccountScreen.kt
└── components/
```

Les ressources utilisateur sont dans chaque module (`res/values/strings.xml`, `values-ar`, `values-en`) ; aucun texte d'interface n'est codé en dur.

---

# D. Modèle de base de données et relations

## D.1 Conventions

- Tables en `snake_case`, noms Kotlin explicites.
- UUID stockés en `TEXT`.
- Montants `INTEGER NOT NULL` en unité mineure.
- Horodatages techniques `INTEGER` epoch millis.
- Dates civiles en ISO `TEXT` (`YYYY-MM-DD`) pour conserver leur sens local.
- Clés étrangères activées ; index explicites ; migrations testées.
- `created_at`, `updated_at`, `deleted_at` sur les données synchronisables.

## D.2 Tables V1

### `workspaces`

- `id` PK UUID
- `name`, `kind` (`PERSONAL`, `BUSINESS`, `FAMILY`, `OTHER`)
- `currency_code`
- `is_active`, `created_at`, `updated_at`, `deleted_at`

Un espace par défaut en V1 ; le modèle permet l'extension.

### `accounts`

- `id` PK, `workspace_id` FK → workspaces
- `name`, `type`, `currency_code`
- `initial_balance_minor` Long
- `color_key`, `icon_key`, `display_order`
- `is_hidden`, `is_archived`
- audit/soft delete

Index : `(workspace_id, is_archived, deleted_at)`, `name`.

### `categories`

- `id` PK, `workspace_id` FK nullable pour catégories système
- `kind` (`EXPENSE`, `INCOME`)
- `name_key` nullable pour traduction système, `custom_name` nullable
- `icon_key`, `color_key`, `is_system`, `is_archived`, `display_order`
- audit/soft delete

### `subcategories`

- `id` PK, `category_id` FK → categories (`RESTRICT`)
- `name_key`/`custom_name`, `is_system`, `is_archived`, `display_order`
- audit/soft delete

### `finance_transactions`

Le nom évite toute ambiguïté avec une transaction SQL.

- `id` PK, `workspace_id` FK
- `type` (`EXPENSE`, `INCOME`, `TRANSFER`)
- `amount_minor` Long strictement positif
- `currency_code`
- `account_id` FK → accounts : compte principal/source
- `destination_account_id` FK nullable → accounts : requis pour transfert
- `category_id` FK nullable → categories
- `subcategory_id` FK nullable → subcategories
- `occurred_at` epoch millis, `local_date` ISO, `zone_id`
- `description`, `note`, `merchant`, `payment_method`
- `is_recurring_instance`, `recurring_transaction_id` FK nullable
- `created_at`, `updated_at`, `deleted_at`

Contraintes métier contrôlées par use case et, lorsque Room le permet proprement, `CHECK` : montant positif ; destination requise et différente pour transfert ; catégorie absente pour transfert ; catégorie requise pour revenu/dépense.

Index :

- `(workspace_id, local_date, deleted_at)`
- `(account_id, occurred_at, deleted_at)`
- `(destination_account_id, occurred_at, deleted_at)`
- `(category_id, local_date, type, deleted_at)`
- `(recurring_transaction_id, local_date)`

### `tags`

- `id` PK, `workspace_id` FK, `name`, `normalized_name`, `color_key`
- contrainte unique `(workspace_id, normalized_name)`
- audit/soft delete

### `transaction_tags`

- `transaction_id` FK, `tag_id` FK
- PK composite `(transaction_id, tag_id)`

### `budgets`

- `id` PK, `workspace_id` FK
- `name`
- `scope` (`GLOBAL`, `CATEGORY`)
- `category_id` FK nullable
- `amount_minor`, `currency_code`
- `period_type` (`MONTHLY` en V1), `start_date`, `end_date` nullable
- `is_active`, audit/soft delete

Index : `(workspace_id, is_active, start_date, deleted_at)`.

### `budget_alert_thresholds`

- `budget_id` FK, `percentage` entier (50, 75, 90, 100)
- `enabled`
- PK composite `(budget_id, percentage)`

### `budget_alert_events`

- `budget_id` FK, `period_key`, `percentage`, `notified_at`
- contrainte unique `(budget_id, period_key, percentage)`

### `recurring_transactions`

- `id` PK, `workspace_id` FK
- modèle d'opération : type, montant, devise, comptes, catégorie, sous-catégorie, texte
- `frequency` (`DAILY`, `WEEKLY`, `MONTHLY`, `BIMONTHLY`, `QUARTERLY`, `YEARLY`, `CUSTOM`)
- `interval_value`, `days_of_week`, `day_of_month`
- `start_date`, `next_due_date`, `end_date` nullable
- `generation_mode` (`AUTO`, `CONFIRM`)
- `is_active`, audit/soft delete

### `recurring_occurrences`

- `id` PK
- `recurring_transaction_id` FK
- `due_date`
- `status` (`PENDING`, `CREATED`, `SKIPPED`, `FAILED`)
- `transaction_id` FK nullable
- `processed_at` nullable
- contrainte unique `(recurring_transaction_id, due_date)`

### `attachments`

Schéma préparé mais fonctionnalité UI V2 : `id`, `transaction_id`, `display_name`, `mime_type`, `relative_path`, `size_bytes`, `sha256`, audit/soft delete. Aucun chemin externe arbitraire n'est traité comme fiable.

### `backup_metadata`

- `id` PK, `created_at`, `format_version`, `app_version`
- `file_name`, `size_bytes`, `checksum`, `encryption_scheme`
- aucune clé ni phrase secrète

## D.3 Paramètres hors Room

`UserSettings` est conceptuellement requis mais stocké dans DataStore : onboarding terminé, pseudonyme, thème, locale, espace actif, devise par défaut, jour de début budgétaire, délai de verrouillage, biométrie activée, masquage écran. Les réglages critiques sont validés avant écriture.

## D.4 Tables V2/V3 réservées au modèle, non créées prématurément

- `goals`, `goal_contributions`
- `scheduled_payments`
- règles de change et taux historiques
- journal de synchronisation et tombstones

Ne pas créer de tables vides « au cas où » réduit les migrations et les contraintes inutiles.

## D.5 Relations

```text
Workspace 1 ── * Account
Workspace 1 ── * Category 1 ── * SubCategory
Workspace 1 ── * FinanceTransaction
Account   1 ── * FinanceTransaction (source/main)
Account   1 ── * FinanceTransaction (destination for transfer)
FinanceTransaction * ── * Tag (via TransactionTag)
Category  1 ── * Budget
RecurringTransaction 1 ── * RecurringOccurrence
RecurringOccurrence 0..1 ── 1 FinanceTransaction
FinanceTransaction 1 ── * Attachment (V2 UI)
Budget 1 ── * BudgetAlertThreshold / BudgetAlertEvent
```

---

# E. Liste des écrans

## Démarrage

1. Splash/verrouillage initial
2. Bienvenue
3. Onboarding — Suivez votre argent
4. Onboarding — Maîtrisez votre budget
5. Onboarding — Préparez l'avenir
6. Onboarding — Vos données restent les vôtres
7. Configuration — profil facultatif
8. Configuration — devise
9. Configuration — premier compte et solde
10. Configuration — début du mois et confirmation

## Navigation principale

11. Accueil / Dashboard
12. Comptes
13. Détail d'un compte
14. Création/modification d'un compte
15. Opérations / historique
16. Détail d'une opération
17. Sélecteur d'ajout rapide
18. Ajout/modification d'une dépense
19. Ajout/modification d'un revenu
20. Ajout/modification d'un transfert
21. Budgets
22. Détail d'un budget
23. Création/modification d'un budget
24. Analyse
25. Détail d'analyse par catégorie/période

## Secondaires V1

26. Recherche universelle
27. Filtres
28. Catégories et sous-catégories
29. Création/modification de catégorie
30. Opérations récurrentes
31. Détail d'une récurrence
32. Création/modification de récurrence
33. Sauvegarde et restauration
34. Export CSV et aperçu des options
35. Paramètres
36. Apparence
37. Devise et format
38. Préférences budgétaires
39. Sécurité — PIN/biométrie/verrouillage
40. Confidentialité et données
41. À propos / licences
42. Écran de saisie PIN
43. Confirmation de suppression (dialogue)
44. Sélecteur de date/période (dialogue ou bottom sheet)
45. Sélecteur de compte/catégorie (bottom sheet recherchable)

## V2

Objectifs, détail/édition d'objectif, échéancier, paiement planifié, calendrier, prévisions, import et mapping CSV, export PDF/XLSX, pièces jointes et capture de reçu.

## V3

Compte cloud, appareils, partage d'espace, conflits de synchronisation, assistant financier avec consentement explicite.

---

# F. Navigation entre les écrans

## Graphe racine

```text
App start
├── Lock (si délai dépassé)
├── OnboardingGraph (si configuration incomplète)
└── MainGraph
    ├── Home
    ├── AccountsGraph
    ├── TransactionsGraph
    ├── BudgetsGraph
    ├── AnalyticsGraph
    └── MoreGraph
```

## Barre inférieure

- Accueil
- Comptes
- Opérations
- Budgets
- Analyse

Le bouton `+` flottant persistant ouvre un bottom sheet Dépense/Revenu/Transfert. La destination courante est conservée lors d'un changement d'onglet. Un nouvel appui sur l'onglet actif remonte au sommet.

## Routes essentielles

```text
home
accounts
accounts/new
accounts/{accountId}
accounts/{accountId}/edit
transactions?accountId=&categoryId=&from=&to=
transactions/new/{type}
transactions/{transactionId}
transactions/{transactionId}/edit
budgets
budgets/new
budgets/{budgetId}
analytics?period=
search
categories
recurring
recurring/{recurringId}
backup
export/csv
settings
settings/security
about
```

Les identifiants passent dans les routes, jamais des objets complets. Le ViewModel recharge la donnée depuis la source de vérité. Les deep links sensibles sont désactivés en V1.

## Menu secondaire

Depuis avatar/menu en haut du Dashboard : Catégories, Récurrences, Sauvegarde, Export, Paramètres, À propos. Les éléments V2 ne sont pas affichés comme faux boutons inactifs.

---

# G. Wireframes textuels des écrans principaux

## G.1 Bienvenue / onboarding

```text
┌────────────────────────────────────┐
│                                    │
│               [B++]                │
│             Budget++               │
│                                    │
│      Bienvenue dans Budget++       │
│ Prenez le contrôle de vos finances │
│             simplement.            │
│                                    │
│       [        Commencer        ]  │
│         Vos données restent ici    │
└────────────────────────────────────┘
```

Pages suivantes : illustration géométrique originale, titre, deux lignes, indicateur `●○○○`, bouton Suivant/Créer mon premier compte et action Passer.

## G.2 Dashboard

```text
┌────────────────────────────────────┐
│ Bonjour                        [⋮] │
│ Solde total                         │
│ 1 245 600,00 DA             [œil] │
│ ┌──────────┐ ┌──────────┐          │
│ │ Revenus  │ │ Dépenses │          │
│ │ +450 000 │ │ -287 000 │          │
│ └──────────┘ └──────────┘          │
│ Disponible              +163 000 DA│
│                                    │
│ Revenus / Dépenses  [Mois ▾]       │
│ [          graphique barres       ]│
│                                    │
│ Mes comptes              Voir tout │
│ [BNA 540k] [Espèces 32k] [Ép. 674k]│
│                                    │
│ Budget du mois             79 %    │
│ [████████████████░░░░]              │
│ 198 000 / 250 000 DA               │
│                                    │
│ Dernières opérations      Voir tout│
│ ⛽ Carburant         −5 800 DA      │
│ ◇ Honoraires       +150 000 DA     │
│       ⌂       □      ◎      ▣     ◒│
│                         [+]        │
└────────────────────────────────────┘
```

Les montants peuvent être masqués. Les cartes sont cliquables avec libellés TalkBack complets.

## G.3 Liste des comptes

```text
┌────────────────────────────────────┐
│ Comptes                       [⋮]  │
│ Total                    1 422 000 │
│                                    │
│ ┌ BNA — Banque ─────────────────┐ │
│ │ 540 000,00 DA        ↗ +2,4 % │ │
│ └───────────────────────────────┘ │
│ ┌ Espèces ──────────────────────┐ │
│ │ 32 000,00 DA                  │ │
│ └───────────────────────────────┘ │
│ ┌ Épargne ──────────────────────┐ │
│ │ 850 000,00 DA                 │ │
│ └───────────────────────────────┘ │
│                         [+ compte] │
└────────────────────────────────────┘
```

Les comptes archivés sont dans un filtre dédié, pas mélangés par défaut.

## G.4 Détail d'un compte

```text
┌────────────────────────────────────┐
│ [←] Compte BNA              [⋮]   │
│ Compte bancaire                    │
│ 540 000,00 DA                      │
│ Initial 400k · Revenus 220k · Dép.80k│
│ [      courbe d'évolution       ]  │
│ [Mois ▾] [Toutes catégories ▾]     │
│ Historique                         │
│ Salaire                 +180 000   │
│ Carburant                 −5 800   │
│ Transfert vers Épargne   −20 000 ↔ │
│                         [+]        │
└────────────────────────────────────┘
```

Menu : modifier, masquer/afficher, archiver, supprimer avec confirmation et règles d'intégrité.

## G.5 Ajout rapide

```text
┌──────────── Bottom sheet ──────────┐
│         Ajouter une opération      │
│                                    │
│ [  −  Dépense ] [ + Revenu ] [↔] │
│                                    │
│ Montant                            │
│ [              5 800,00        DA]│
│ Catégorie  [Transport > Carburant] │
│ Compte     [Espèces              ▾]│
│ Description [Plein de carburant   ]│
│ [⌄ Plus de détails]                │
│                                    │
│ [          Enregistrer           ] │
└────────────────────────────────────┘
```

Pour un transfert, Catégorie devient Compte destination. La validation signale précisément le champ sans effacer la saisie.

## G.6 Historique des opérations

```text
┌────────────────────────────────────┐
│ Opérations               [🔍] [⚙] │
│ [Toutes] [Dépenses] [Revenus] [↔] │
│ Août 2026        Solde net +163 000│
│ Aujourd'hui                         │
│ ⛽ Carburant · Espèces    −5 800 DA│
│ 🍽 Restaurant · BNA       −3 200 DA│
│ 20 août                           │
│ ◇ Honoraires · BNA      +150 000 DA│
│                                    │
│       navigation             [+]  │
└────────────────────────────────────┘
```

La liste est paginée et groupée par jour. Les filtres actifs apparaissent en chips supprimables.

## G.7 Budgets

```text
┌────────────────────────────────────┐
│ Budgets                [Août 2026 ▾]│
│ Budget global                      │
│ 198 000 / 250 000 DA          79 % │
│ [████████████████░░░░]              │
│ Reste 52 000 · 10 jours            │
│ Conseil 5 200 DA / jour            │
│                                    │
│ Par catégorie                      │
│ Alimentation 37 500 / 50 000  75 % │
│ Transport   42 000 / 60 000   70 % │
│ Loisirs     19 000 / 20 000   95 % │
│                           [+ budget]│
└────────────────────────────────────┘
```

Les couleurs de progression ne sont jamais l'unique vecteur de sens ; le pourcentage et le reste sont toujours lisibles.

## G.8 Analyse

```text
┌────────────────────────────────────┐
│ Analyse                  [Mois ▾]  │
│ [Août 2026]                          │
│ Dépenses 287k  Revenus 450k        │
│ Épargne 163k   Taux 36,2 %         │
│ [      revenus vs dépenses       ] │
│                                    │
│ Dépenses par catégorie             │
│ [ anneau accessible + légende ]    │
│ Transport       85 000       34 %  │
│ Alimentation    52 000       21 %  │
│ Logement        45 000       18 %  │
│                                    │
│ vs juillet : Dép. −12 % · Rev. +5 %│
└────────────────────────────────────┘
```

Chaque graphique possède une alternative textuelle complète pour TalkBack.

## G.9 Recherche et filtres

```text
┌────────────────────────────────────┐
│ [←] [Rechercher montant, mot…    ] │
│ [Date] [Compte] [Catégorie] [Type] │
│ Filtres actifs : Août · Dépense ×  │
│ 12 résultats · Total −96 400 DA    │
│ ... liste paginée ...              │
└────────────────────────────────────┘
```

Le panneau Filtres permet date, compte, catégorie, type, minimum, maximum et tags, avec Réinitialiser/Appliquer.

## G.10 Paramètres et sécurité

```text
┌────────────────────────────────────┐
│ Paramètres                         │
│ Profil et préférences              │
│ Devise                  DZD        │
│ Début du mois           1          │
│ Apparence               Système    │
│ Sécurité                           │
│ Verrouillage             Activé    │
│ Biométrie                Activée   │
│ Verrouillage auto        2 min     │
│ Données                             │
│ Sauvegarder · Restaurer · Exporter │
│ Confidentialité · À propos         │
└────────────────────────────────────┘
```

---

# H. Design System proposé

## H.1 Identité

Concept : monogramme original **B++** où les deux signes plus forment deux paliers ascendants. Il évoque la progression sans reprendre portefeuille, banque ou graphisme d'un produit existant. Une version compacte ne conserve que le `B` et les deux paliers.

Slogan stocké comme ressource modifiable : « Votre argent, plus clair. Votre budget, mieux maîtrisé. »

## H.2 Couleurs

Palette de départ, à valider sur maquettes et tests WCAG :

| Token | Clair | Sombre | Usage |
|---|---|---|---|
| Primary | `#176B5B` | `#72D6BE` | actions, marque |
| OnPrimary | `#FFFFFF` | `#00382E` | contenu sur primaire |
| Secondary | `#4D635D` | `#B4CCC4` | composants secondaires |
| Tertiary | `#365E8D` | `#A5C8F8` | analyse/information |
| Background | `#F7FAF8` | `#101412` | fond |
| Surface | `#FFFFFF` | `#181D1A` | cartes |
| Expense | `#B3261E` | `#FFB4AB` | dépense (avec signe/libellé) |
| Income | `#087A55` | `#68DBAC` | revenu (avec signe/libellé) |
| Warning | `#8A4F00` | `#FFB95C` | seuils |

Les palettes dynamiques Android peuvent être proposées, mais l'identité Budget++ reste le défaut. Les contrastes texte/fond visent WCAG AA ; le vert/rouge n'est jamais le seul indicateur.

## H.3 Typographie

- Police système Android/Roboto pour performance et couverture arabe.
- `DisplaySmall` pour solde global.
- `HeadlineSmall` pour titres d'écran.
- `TitleMedium` pour cartes.
- `BodyLarge/Medium` pour contenu.
- `LabelLarge` pour actions.
- Chiffres tabulaires lorsque disponibles ; respect intégral du facteur de police Android.

## H.4 Formes, espacement et élévation

- Grille 4 dp ; espacements usuels 8/12/16/24/32 dp.
- Marge écran 16 dp téléphone, 24 dp tablette.
- Cartes 20 dp, champs 12 dp, chips 50 %.
- Cibles tactiles minimales 48 × 48 dp.
- Élévation limitée : 0/1/3 ; préférence aux contours et contrastes de surface.

## H.5 Composants Budget++

- `BudgetPlusTopBar`
- `BalanceHeroCard`
- `MoneyText` (signe, confidentialité, sémantique)
- `MetricCard`
- `AccountCard`
- `TransactionRow`
- `BudgetProgressCard`
- `PeriodSelector`
- `CategoryIcon`
- `EmptyState` / `ErrorState`
- `QuickAddBottomSheet`
- `FinancialBarChart`, `CategoryDonutChart` avec description accessible
- `ConfirmDestructiveDialog`
- `SensitiveContentOverlay`

## H.6 Mouvement

Animations de 150–300 ms, sans bloquer la saisie : transition de progression, apparition de carte, retour haptique léger sur enregistrement. Respect de la préférence système de réduction des animations. Pas de confettis sur une opération financière courante.

## H.7 Icônes et accessibilité

Material Symbols peut servir aux actions génériques selon sa licence ; les icônes de marque sont originales. Toutes les actions ont une description localisée. Les informations graphiques ont une liste textuelle équivalente. L'UI est testée à 200 % de taille de police, en RTL et avec contraste élevé.

---

# I. Roadmap MVP

| Phase | Livrable vérifiable | Sortie attendue |
|---:|---|---|
| 1 | Architecture générale | Ce document + ADR initiales |
| 2 | Projet et Gradle | Projet ouvrable, CI, variantes debug/release |
| 3 | Design system | Thèmes clair/sombre, composants, previews |
| 4 | Base Room | Entités, DAO, migrations, base de test |
| 5 | Domaine/repositories | Modèles, mappers, use cases financiers |
| 6 | Comptes | CRUD, archivage, solde initial |
| 7 | Transactions | Dépense, revenu, transfert atomique, historique |
| 8 | Dashboard | Agrégats, comptes, opérations récentes, graphique |
| 9 | Catégories | Jeu DZD/français, CRUD et sous-catégories |
| 10 | Budgets | Global/catégorie, calcul, seuils |
| 11 | Récurrences | Règles, idempotence, WorkManager |
| 12 | Statistiques | Indicateurs, catégories, comparaisons |
| 13 | Recherche/filtres | Requête combinée paginée |
| 14 | Sauvegarde/export | Archive chiffrée, restauration, CSV |
| 15 | Sécurité | PIN, biométrie, délai, confidentialité |
| 16 | Tests | Unitaires, intégration, UI, sécurité de base |
| 17 | Optimisation | Benchmarks 10k/50k/100k, accessibilité, RTL |
| 18 | Google Play | AAB, icône, privacy, licences, checklist release |

## Jalons de livraison

1. **Fondations** (phases 2–5) : compilation et tests de domaine/Room.
2. **Boucle financière** (6–9) : première utilisation réellement exploitable.
3. **Pilotage** (10–13) : budgets, récurrences, analyses, recherche.
4. **Confiance** (14–17) : portabilité, sécurité, qualité et performance.
5. **Publication** (18) : build release reproductible et dossier Play.

Chaque phase doit avoir : critères d'acceptation, code compilé, tests pertinents, revue des chaînes localisées et note de migration si le schéma change.

---

# J. Fonctionnalités précises de la version 1.0

## Inclus

- Onboarding en français et configuration initiale.
- Architecture de localisation prête pour anglais/arabe et RTL.
- Devise DZD par défaut ; EUR, USD, CAD, GBP, CHF, MAD, TND, AED et devise personnalisée pour un compte. Pas de conversion entre devises.
- Un espace personnel actif ; modèle préparé pour plusieurs espaces.
- Comptes illimités : types, solde initial, affichage, modification, masquage, archivage.
- Revenus, dépenses et transferts atomiques.
- Saisie rapide + détails facultatifs hors pièces jointes/localisation.
- Catégories/sous-catégories par défaut et personnalisées.
- Tags simples, modes de paiement et commerçant.
- Historique paginé, détail, modification, suppression logique confirmée.
- Recherche par texte, montant, compte, catégorie, date, commerçant, description, tag.
- Filtres combinés et totaux du résultat.
- Dashboard : solde, revenus/dépenses/disponible du mois, comptes, budget, dernières opérations.
- Graphiques barres et anneau sur périodes standard ; courbe si validée pendant la phase statistiques.
- Budgets mensuels global et par catégorie ; seuils 50/75/90/100 et notifications locales autorisées.
- Récurrences quotidiennes, hebdomadaires, mensuelles, bimestrielles, trimestrielles, annuelles et intervalle simple.
- Statistiques : totaux, solde, épargne, taux, moyennes, catégorie principale, comparaison mensuelle.
- Export CSV via Storage Access Framework.
- Sauvegarde/restauration locale chiffrée, versionnée et vérifiée.
- Paramètres de devise, début du mois, thème clair/sombre/système.
- PIN, biométrie, verrouillage automatique et masquage facultatif des montants/captures.
- États vides/erreurs, accessibilité TalkBack, tailles de texte, RTL structurel.
- Tests automatiques et préparation AAB.

## Explicitement hors V1

- Conversion multidevise et taux de change.
- Objectifs d'épargne, calendrier et échéancier avancé.
- Prévision de trésorerie.
- Pièces jointes, reçus et localisation.
- Imports bancaire/CSV et export XLSX/PDF.
- Synchronisation, comptes en ligne et partage.
- Offre Premium effective, paiement, publicité et assistant IA.

Cette frontière évite de promettre une fonction visuelle sans garanties comptables ou de sécurité.

---

# K. Risques techniques identifiés

| Risque | Impact | Réduction |
|---|---|---|
| Erreur de signe/agrégat/transfert | Solde faux, critique | invariants métier, SQL revu, tests propriété et jeux de référence |
| Arrondis/devises à décimales variables | Montants faux | Long minoritaire, métadonnées de fraction, tests limites |
| Périodes, fuseaux, changement d'heure/mois | Mauvais rattachement | `LocalDate` + zone explicite, horloge injectable, tests DST/fin de mois |
| Doublons de récurrence | Dépenses répétées | clé unique d'occurrence, transaction Room, worker idempotent |
| Perte pendant restauration | Perte totale | validation préalable, restauration vers fichier temporaire, swap atomique, rollback |
| Fausse sécurité du PIN | Exposition | Keystore, KDF, revue sécurité, modèle de menace documenté |
| Base ou export exposé par sauvegarde système | Fuite | auto-backup exclu, SAF explicite, chiffrement d'archive |
| Performances à 100k lignes | UI lente | index, projections, Paging, benchmarks Macrobenchmark |
| Migrations Room | crash/perte après mise à jour | schémas exportés, tests de toutes migrations, pas de destructive migration |
| Recherche libre coûteuse | latence | requêtes bornées, debounce, index/FTS après mesure |
| Graphiques Compose accessibles | exclusion TalkBack | descriptions textuelles et tableau alternatif |
| RTL/arabe ajouté trop tard | refonte UI | start/end, ressources dès le début, pseudo-locales/RTL en CI |
| WorkManager non ponctuel | échéance tardive | rattrapage au lancement, statut clair, ne pas garantir l'heure exacte |
| Notifications refusées | alertes invisibles | alertes in-app persistantes ; permission demandée contextuellement |
| Bibliothèque non compatible commercialement | risque légal | inventaire SBOM/licences, préférer Apache-2.0/BSD/MIT, revue avant ajout |
| Architecture trop modulaire | builds/maintenance | modules par responsabilité stable, convention plugins |
| Future synchronisation | conflits/pertes | ne pas l'improviser ; protocole dédié V3, UUID/audit dès V1 |
| Données sensibles dans logs/crash reports | fuite | redaction, aucun SDK de télémétrie par défaut, tests/revue release |

---

# L. Plan de tests

## L.1 Pyramide

1. **Unitaires domaine** : rapides, JVM, majorité de la couverture utile.
2. **Intégration Room/repositories** : base réelle en mémoire et fichiers de migration.
3. **ViewModel** : flux d'états, concurrence et erreurs.
4. **UI Compose** : parcours critiques et accessibilité sémantique.
5. **Instrumentés système** : Biometric abstrait, WorkManager, SAF, sauvegarde.
6. **Performance et release** : benchmark, installation/mise à jour, AAB.

La couverture chiffrée n'est pas un objectif isolé ; 100 % des invariants financiers critiques doivent avoir des tests explicites.

## L.2 Matrice métier

### Argent et soldes

- solde initial positif, nul et négatif autorisé selon type ;
- revenu/dépense, modification, suppression/restauration logique ;
- transfert source/destination, même compte refusé, atomicité sur erreur ;
- valeurs proches des limites de `Long`, dépassement détecté ;
- DZD et devises à 0/2/3 unités mineures ;
- interdiction d'additionner des devises différentes.

### Budgets/statistiques

- bornes 49,99/50/75/90/100/>100 % ;
- alerte unique par période/seuil ;
- global vs catégorie ; transfert exclu ; suppression prise en compte ;
- mois budgétaire commençant les 1, 15, 28–31 avec règle documentée ;
- mois vide, comparaison avec zéro, taux d'épargne négatif.

### Récurrences/dates

- fin de mois (31 → dernier jour ou règle choisie), février bissextile ;
- changement d'heure, fuseau, année ;
- rattrapage après appareil éteint ;
- deux exécutions concurrentes sans doublon ;
- pause, reprise, fin et occurrence ignorée.

### Recherche

- accents, casse, apostrophes, `%`/`_`, arabe, espaces ;
- combinaisons de filtres et limites inclusives ;
- montant au format affiché ; pagination stable après modification.

## L.3 Base de données

- contraintes FK et cascade/restrict attendus ;
- requêtes d'agrégats comparées à un oracle Kotlin sur jeux aléatoires ;
- migrations de chaque version N vers N+1 et anciennes versions supportées ;
- restauration d'une base corrompue refusée sans écraser l'existante ;
- concurrence lecture/écriture ; 100k transactions synthétiques.

## L.4 ViewModels et UI

- chargement/contenu/vide/erreur/réessai ;
- double clic Enregistrer sans doublon ;
- rotation et recréation de processus via `SavedStateHandle` pour formulaire ;
- navigation retour sans perte non confirmée ;
- saisie rapide chronométrée sur scénarios usuels ;
- TalkBack : ordre, rôle, montant et tendance annoncés ;
- police 200 %, petits écrans, tablette, clair/sombre, contraste ;
- RTL forcé et pseudo-locales ; clavier numérique et formats locaux.

## L.5 Sauvegarde, export et sécurité

- round-trip sauvegarde/restauration avec checksum identique ;
- mauvais mot de passe, archive tronquée, version future, manque d'espace ;
- nonce/sel uniques et absence de secret dans métadonnées/logs ;
- CSV : échappement virgule/point-virgule/guillemet/saut de ligne, UTF-8 BOM configurable, arabe ;
- verrouillage après délai, arrière-plan, redémarrage, échecs PIN ;
- biométrie indisponible, annulée, verrouillée, nouvel enrôlement ;
- inspection manifeste : permissions minimales, backups et composants exportés.

## L.6 Performance

Scénarios 10k/50k/100k :

- ouverture dashboard ;
- première page historique et défilement ;
- recherche filtrée ;
- statistiques mensuelles/annuelles ;
- insertion et modification ;
- taille/temps de sauvegarde.

Des budgets mesurables seront fixés sur appareil de référence en phase 17 (par exemple absence de frame bloquée lors du scroll et requêtes usuelles sous le seuil perceptible), plutôt que de déclarer des chiffres non mesurés dès la conception.

## L.7 Validation manuelle avant publication

- installation propre et mise à jour depuis dernière version publiée ;
- mode avion complet ;
- parcours onboarding → compte → dépense → transfert → budget → export ;
- politique de confidentialité cohérente avec les permissions et bibliothèques ;
- icône adaptative, nom Budget++, captures et textes Play ;
- build release signé via secret externe, aucun keystore dans Git ;
- `lint`, tests, analyse de dépendances/licences et scan de secrets réussis.

---

# Critères d'acceptation de la conception

Avant la phase 2, les décisions suivantes doivent être considérées comme validées ou amendées :

1. stockage monétaire en unités mineures ;
2. transfert unique avec source/destination ;
3. DataStore pour réglages et Room pour finance ;
4. un espace actif et aucune conversion multidevise en V1 ;
5. périmètre V1/V2 ;
6. sauvegarde volontaire chiffrée et absence de cloud ;
7. palette/identité de départ ;
8. structure multi-module pragmatique.

Une fois ces points validés, la prochaine étape est **Phase 2 — initialisation du projet Android, catalogue de versions, plugins conventionnels, CI et premier build compilable**, sans encore implémenter les écrans métier.
