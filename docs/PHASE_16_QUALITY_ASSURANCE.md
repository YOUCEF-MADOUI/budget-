# Phase 16 — Tests complets et assurance qualité

## Stratégie

La pyramide de tests couvre désormais les règles Kotlin pures, Room sous Robolectric, les repositories avec une base réelle en mémoire et les principaux écrans Compose. Les tests financiers utilisent exclusivement des montants `Long` en unité mineure.

## Domaine

Les suites vérifient les invariants dépense/revenu/transfert, limites `Long`, catégories compatibles, réaffectations, budgets et seuils, moyennes analytiques, récurrences sur mois courts et DST, recherche, chiffrement, restauration atomique, format PIN, blocage progressif et verrouillage de cycle de vie.

## Room, DAO et migrations

`DaoIntegrationTest` ouvre la vraie base Room et contrôle : soldes après dépense/revenu/transfert, agrégats excluant les transferts et suivi réactif d'un budget de catégorie.

`FullMigrationTest` crée un schéma version 1 peuplé, applique successivement `MIGRATION_1_2` à `MIGRATION_7_8`, puis laisse Room valider le schéma 8 complet. Il vérifie notamment sous-catégories, budgets, récurrences et FTS. Les convertisseurs et déclarations de versions restent testés séparément.

## Intégration repositories

Une base Room en mémoire exécute les repositories réels pour les parcours : création de deux comptes, dépense, revenu, transfert et vérification exacte des soldes ; création d'un budget puis mise à jour par une dépense.

## UI Compose

Les tests Robolectric Compose couvrent la création d'un compte, la saisie d'une dépense et la création d'un budget global. Les états vides sont vérifiés en français et anglais. Un test arabe confirme le libellé localisé et `LayoutDirection.Rtl`. Le Design System vérifie que les montants masqués exposent une description d'accessibilité localisée.

Les tests de sauvegarde couvrent formats 1/2, mauvais mot de passe, altération AES-GCM, commit et rollback. Les tests de sécurité couvrent PIN, temporisations et arrière-plan. Les récurrences couvrent ancrages et identifiants idempotents.

## Lint et analyse statique en CI

Les convention plugins font dépendre chaque `assembleDebug` Android de `lintDebug` avec erreurs bloquantes et rapports XML/HTML. Le workflow existant exécute donc Android Lint pendant son étape de build sans nécessiter une étape divergente.

La tâche racine `staticAnalysis` est une dépendance de toutes les tâches de test. Elle refuse les appels `android.util.Log`, `println`, les textes Compose codés en dur et les fichiers de clés/keystores versionnés. La CI exécute cette analyse pendant `./gradlew test`.

## Commandes validées

```bash
./gradlew assembleDebug --stacktrace
./gradlew test --stacktrace
```

`assembleDebug` inclut Lint ; `test` inclut l'analyse statique, les tests JVM, Robolectric et Compose.
