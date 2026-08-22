# Budget++

**Votre argent, plus clair. Votre budget, mieux maîtrisé.**

Budget++ est une application Android native de gestion budgétaire personnelle, conçue pour fonctionner hors ligne et protéger les données financières de l'utilisateur.

## État du projet

- **Phase 1 — Conception :** terminée. Le [dossier de conception fonctionnelle et technique](docs/CONCEPTION.md) reste la source de vérité.
- **Phase 2 — Fondation Android :** terminée et validée par la CI.
- **Phase 3 — Design System :** terminée.
- **Phase 4 — Room et premières fonctionnalités :** terminée.
- **Phase 5 — Domaine et boucle financière utilisable :** navigation principale, validations, confirmations, erreurs et historique filtrable en français, anglais et arabe.
- **Phase 8 — Tableau de bord financier :** agrégats Room réactifs, soldes, évolution, catégories, opérations récentes et périodes personnalisées.
- **Phase 9 — Catégories avancées :** sous-catégories, icônes, couleurs, recherche et réaffectation transactionnelle sûre.
- **Phase 10 — Budgets :** budgets globaux/par catégorie, périodes, seuils et suivi Room réactif avec historique.
- **Phase 11 — Opérations récurrentes :** règles quotidiennes à annuelles, WorkManager, rattrapage et idempotence.
- **Phase 12 — Analyse :** évolutions, comparaisons, moyennes et catégories principales via Room.
- **Phase 13 — Recherche :** filtres combinés, FTS Unicode et pagination keyset conçue pour 100 000 opérations.

Documentation : [Design System Budget++](docs/PHASE_3_DESIGN_SYSTEM.md) · [Base Room et premières fonctionnalités](docs/PHASE_4_FIRST_FEATURES.md) · [Domaine et boucle financière utilisable](docs/PHASE_5_USABLE_FINANCE_LOOP.md) · [Tableau de bord financier](docs/PHASE_8_DASHBOARD.md) · [Catégories avancées](docs/PHASE_9_CATEGORIES.md) · [Gestion des budgets](docs/PHASE_10_BUDGETS.md) · [Opérations récurrentes](docs/PHASE_11_RECURRING.md) · [Statistiques et analyses](docs/PHASE_12_ANALYTICS.md) · [Recherche et filtres avancés](docs/PHASE_13_SEARCH.md).

## Configuration Android

| Paramètre | Valeur | Justification |
|---|---:|---|
| `minSdk` | 26 (Android 8.0) | Couverture large avec un socle moderne et maintenable |
| `compileSdk` | 36 | API Android stable compatible avec la chaîne AGP retenue |
| `targetSdk` | 36 | Comportements de confidentialité et de sécurité Android actuels |
| Java | 17 | Version requise et supportée par Android Gradle Plugin |
| Application ID | `com.budgetplusplus.app` | Identifiant provisoire centralisé dans le convention plugin |
| Version | `1 / 0.1.0` | Première version de développement, pas une V1 publique |

## Prérequis

- Android Studio récent compatible avec AGP 8.13 (ou IntelliJ avec les plugins Android/Kotlin appropriés)
- JDK 17
- Android SDK Platform 36 et Build Tools associés
- Aucun fichier `local.properties` versionné : Android Studio le crée avec le chemin local du SDK

Le Gradle Wrapper fourni est la seule installation Gradle requise.

## Construire et tester

Depuis la racine du dépôt :

```bash
./gradlew projects
./gradlew assembleDebug
./gradlew test
```

L'APK de développement est produit dans `app/build/outputs/apk/debug/`. Le variant debug utilise l'identifiant `com.budgetplusplus.app.debug` afin de pouvoir cohabiter avec une future version de production.

Pour installer sur un appareil ou émulateur connecté :

```bash
./gradlew installDebug
```

## Architecture des modules

```text
:app                         point d'entrée, Hilt et navigation racine
├── :feature:onboarding      écran de validation technique / futur onboarding
├── :feature:dashboard       destination temporaire / futur dashboard
├── :feature:accounts        comptes et soldes locaux
├── :feature:categories      catégories système/personnalisées
├── :feature:transactions    dépenses, revenus et transferts
├── :feature:*               frontières UI des fonctionnalités suivantes
├── :core:designsystem       thème Compose clair/sombre minimal
├── :core:ui                 composants UI partagés futurs
├── :data                    implémentations des repositories locaux, DataStore et workers futurs
├── :database                base Room/KSP, entités, DAO et schémas versionnés
├── :domain                  règles métier Kotlin pur
├── :core:model              modèles partagés
├── :core:common             infrastructure Kotlin commune
├── :core:security           frontière Keystore/verrouillage future
└── :core:testing            fakes et fixtures partagés futurs
```

Les features dépendent uniquement des modules core autorisés. Les modules core ne dépendent d'aucune feature. Les modules volontairement vides matérialisent une frontière d'architecture sans introduire prématurément des modèles métier.

Le nom `:feature:analytics` suit `docs/CONCEPTION.md`; « Analyse »/« Statistiques » désigne la même zone fonctionnelle côté produit.

## Build logic

`build-logic` contient des convention plugins pragmatiques :

- `budgetplusplus.android.application`
- `budgetplusplus.android.library`
- `budgetplusplus.android.compose`
- `budgetplusplus.android.feature`
- `budgetplusplus.android.room`
- `budgetplusplus.android.hilt`
- `budgetplusplus.kotlin.library`

Ils centralisent les SDK, Java/Kotlin 17, Compose, namespaces, tests et traitements KSP. Les versions des plugins et bibliothèques résident dans `gradle/libs.versions.toml`.

## Première application

Le lancement affiche un écran Budget++ localisé en français, anglais et arabe. Le bouton **Commencer** ouvre un écran technique depuis lequel le catalogue interne du Design System est accessible. Ce catalogue permet de vérifier palette, typographie, composants, thèmes clair/sombre, RTL et données financières fictives. Ces destinations seront retirées de la navigation utilisateur avant publication.

## Confidentialité et secrets

- La sauvegarde automatique Android est désactivée et les domaines financiers sont exclus des transferts système.
- Aucun secret, keystore, token ou `local.properties` ne doit être committé.
- La configuration de signature de production sera externe au dépôt.
- Aucune permission réseau n'est déclarée pour l'application à ce stade.

## Intégration continue

`.github/workflows/android.yml` valide le wrapper, liste les projets, construit l'application debug et exécute les tests unitaires sur chaque pull request vers `main` et sur les branches suivies.

## Prochaine phase

**Phase 4 — Base Room** : entités, relations, DAO, index, convertisseurs, migrations et tests de base conformément au modèle validé, sans coupler la persistance aux composants UI.
