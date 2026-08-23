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
- **Phase 14 — Sauvegarde/export :** archives AES-GCM versionnées, restauration atomique et exports CSV UTF-8 via SAF.
- **Phase 15 — Sécurité :** PIN Keystore, biométrie, verrouillage de cycle de vie, limitation et FLAG_SECURE.
- **Phase 16 — Assurance qualité :** domaine, Room 1→11, repositories, UI Compose, locales, accessibilité, Lint et analyse statique.
- **Phase 17 — Optimisation :** charges 10k/50k/100k, index keyset, mémoire bornée, cache monétaire, taille APK et audit RTL.
- **Phase 17 — Édition des opérations :** modification atomique, recalcul des soldes, détail compte et création rapide de catégories.
- **Phase 18 — Détail des comptes :** paramètres, historique filtré, actions rapides, déplacement atomique et suppression sûre.
- **Phase 19 — Comptes hiérarchiques :** profondeur libre, soldes consolidés, cycles bloqués et réaffectation sûre des branches.
- **Phase 20 — Échéances :** dépenses futures, paiements partiels idempotents, annulation et rappels locaux.
- **Phase 21 — Favoris et médias :** photos privées WebP, icônes localisées, quantités exactes et clics rapides idempotents.
- **Phase 22 — Budget++ 1.0 :** identité officielle adaptative et monochrome, finition release, validation intégrale et procédure de signature sécurisée.

Documentation : [Design System Budget++](docs/PHASE_3_DESIGN_SYSTEM.md) · [Base Room et premières fonctionnalités](docs/PHASE_4_FIRST_FEATURES.md) · [Domaine et boucle financière utilisable](docs/PHASE_5_USABLE_FINANCE_LOOP.md) · [Tableau de bord financier](docs/PHASE_8_DASHBOARD.md) · [Catégories avancées](docs/PHASE_9_CATEGORIES.md) · [Gestion des budgets](docs/PHASE_10_BUDGETS.md) · [Opérations récurrentes](docs/PHASE_11_RECURRING.md) · [Statistiques et analyses](docs/PHASE_12_ANALYTICS.md) · [Recherche et filtres avancés](docs/PHASE_13_SEARCH.md) · [Sauvegarde, restauration et export](docs/PHASE_14_BACKUP_EXPORT.md) · [Sécurité de l’application](docs/PHASE_15_SECURITY.md) · [Tests et assurance qualité](docs/PHASE_16_QUALITY_ASSURANCE.md) · [Optimisation, performances et accessibilité](docs/PHASE_17_OPTIMIZATION.md) · [Modification des opérations](docs/PHASE_17_TRANSACTION_EDITING.md) · [Détail des comptes et réaffectation](docs/PHASE_18_ACCOUNT_DETAILS.md) · [Comptes hiérarchiques](docs/PHASE_19_HIERARCHICAL_ACCOUNTS.md) · [Échéances et paiements futurs](docs/PHASE_20_FUTURE_PAYMENTS.md) · [Favoris, médias et opérations rapides](docs/PHASE_21_FAVORITES_AND_MEDIA.md) · [Finalisation Budget++ 1.0](docs/PHASE_22_RELEASE_1_0.md).

## Configuration Android

| Paramètre | Valeur | Justification |
|---|---:|---|
| `minSdk` | 26 (Android 8.0) | Couverture large avec un socle moderne et maintenable |
| `compileSdk` | 36 | API Android stable compatible avec la chaîne AGP retenue |
| `targetSdk` | 36 | Comportements de confidentialité et de sécurité Android actuels |
| Java | 17 | Version requise et supportée par Android Gradle Plugin |
| Application ID | `com.budgetplusplus.app` | Identifiant stable centralisé dans le convention plugin |
| Version | `10000 / 1.0.0` | Première version stable Budget++ |

## Prérequis

- Android Studio récent compatible avec AGP 8.13 (ou IntelliJ avec les plugins Android/Kotlin appropriés)
- JDK 17
- Android SDK Platform 36 et Build Tools associés
- Aucun fichier `local.properties` versionné : Android Studio le crée avec le chemin local du SDK

Le Gradle Wrapper fourni est la seule installation Gradle requise.

## Construire et tester

Depuis la racine du dépôt :

```bash
./gradlew projects --stacktrace
./gradlew clean assembleDebug --stacktrace
./gradlew :app:releaseCandidate --stacktrace
./gradlew lint staticAnalysis test --stacktrace
```

La tâche `:app:releaseCandidate` valide ensemble l'APK debug et le bundle release. Les sorties sont :

- APK de test installable : `app/build/outputs/apk/debug/app-debug.apk` ;
- AAB release non signé : `app/build/outputs/bundle/release/app-release.aab`.

Le variant debug utilise l'identifiant `com.budgetplusplus.app.debug`, afin de cohabiter avec la version de production. Pour installer et lancer sur un appareil ou émulateur connecté :

```bash
./gradlew installDebug
adb shell monkey -p com.budgetplusplus.app.debug -c android.intent.category.LAUNCHER 1
```

Parcours manuel recommandé : compte parent/enfant, soldes propre/consolidé, dépense/revenu/transfert, modification, budget, échéance et paiements partiels, récurrence, favori avec plusieurs clics, photo, statistiques/recherche, sauvegarde/restauration, PIN et biométrie. Vérifier ensuite FR/EN/AR RTL, clair/sombre, petit écran, tablette et grandes polices. Voir le [dossier de release 1.0](docs/PHASE_22_RELEASE_1_0.md) pour la signature sécurisée et la checklist complète.

## Architecture des modules

```text
:app                         point d'entrée, Hilt et navigation racine
├── :feature:onboarding      accueil localisé et identité officielle
├── :feature:dashboard       tableau de bord financier
├── :feature:accounts        comptes et soldes locaux
├── :feature:categories      catégories système/personnalisées
├── :feature:transactions    dépenses, revenus et transferts
├── :feature:*               frontières UI des fonctionnalités suivantes
├── :core:designsystem       thème Compose clair/sombre minimal
├── :core:ui                 composants UI partagés et médias
├── :data                    repositories locaux, DataStore, sauvegarde et workers
├── :database                base Room/KSP, entités, DAO et schémas versionnés
├── :domain                  règles métier Kotlin pur
├── :core:model              modèles partagés
├── :core:common             infrastructure Kotlin commune
├── :core:security           contrats Keystore et verrouillage
└── :core:testing            fakes et fixtures partagés
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

Le lancement affiche l'identité officielle Budget++ et un accueil localisé en français, anglais et arabe. Le bouton **Commencer** ouvre le tableau de bord financier. Le catalogue interne du Design System reste disponible dans le code pour les tests de composants, mais n'est plus exposé dans la navigation de production.

## Confidentialité et secrets

- La sauvegarde automatique Android est désactivée et les domaines financiers sont exclus des transferts système.
- Aucun secret, keystore, token ou `local.properties` ne doit être committé.
- La configuration de signature de production sera externe au dépôt.
- Aucune permission réseau n'est déclarée pour l'application à ce stade.

## Intégration continue

`.github/workflows/android.yml` valide le wrapper, liste les projets, construit l'application debug et exécute les tests unitaires sur chaque pull request vers `main` et sur les branches suivies.

## Publication

Budget++ est préparé en version `1.0.0`. La signature de production, le test sur appareil physique et la validation via Play Internal Testing restent volontairement externes au dépôt : aucun keystore ni secret de publication n'est versionné.
