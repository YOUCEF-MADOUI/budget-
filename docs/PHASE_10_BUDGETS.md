# Phase 10 — Gestion des budgets

## Fonctionnalités

- budgets globaux et budgets associés à une catégorie de dépense ;
- périodes mensuelles et périodes personnalisées inclusives ;
- création et modification du nom, montant, périmètre, période et seuil ;
- suivi réactif des dépenses par Room ;
- montant prévu, dépensé et restant en unité mineure ;
- progression accessible avec états normal, proche de la limite et dépassé ;
- seuil d'alerte configurable entre 50 et 99 % et seuil de dépassement à 100 % ;
- archivage, restauration et historique conservant les résultats de la période ;
- accès direct depuis le tableau de bord financier.

## Calculs

Les montants sont stockés et agrégés en `Long`. `BudgetDao` calcule directement dans SQLite la somme des dépenses non supprimées comprises entre les dates du budget. Un budget de catégorie ajoute un filtre sur `category_id`, tandis qu'un budget global agrège toutes les dépenses. Les transferts et revenus sont exclus.

Room observe les tables `budgets`, `budget_alert_thresholds`, `finance_transactions` et `categories`. Une nouvelle dépense, sa suppression logique ou sa réaffectation provoque donc immédiatement une nouvelle émission du `Flow` de progression.

Les comparaisons de seuil utilisent `BigInteger` afin d'éviter tout débordement des multiplications de pourcentage. Les conversions en `Float` restent limitées à la barre de progression du Design System.

## Room et migration

Le schéma passe de la version 3 à la version 4 avec `MIGRATION_3_4` :

- table `budgets` et index de période/état/catégorie ;
- table `budget_alert_thresholds` avec clé composite ;
- table `budget_alert_events` préparée pour l'idempotence des notifications locales ;
- clés étrangères vers l'espace, la catégorie et le budget.

La migration ne modifie aucune donnée financière existante.

## Architecture

- `BudgetProgress` et `BudgetInput` dans `core:model` ;
- `BudgetValidator` dans le domaine ;
- `BudgetRepository` indépendant d'Android ;
- `LocalBudgetRepository` transactionnel ;
- `BudgetDao` pour les agrégats réactifs ;
- `BudgetsViewModel` et `BudgetsScreen` dans `feature:budgets`.

## Internationalisation et accessibilité

Toutes les chaînes existent en français, anglais et arabe. Les sélecteurs, calendriers, listes, cartes et dialogues utilisent Compose et respectent RTL. Les états ne reposent jamais uniquement sur une couleur : un texte Normal, Proche de la limite ou Dépassé accompagne la barre et le montant restant.

## Vérification manuelle

1. Depuis le tableau de bord, ouvrir **Budgets**.
2. Créer un budget global mensuel et un budget mensuel Alimentation.
3. Saisir des dépenses et vérifier la mise à jour immédiate des deux budgets.
4. Modifier le seuil pour traverser les états normal et proche de la limite.
5. Dépasser le montant et vérifier l'état dépassé et le restant négatif.
6. Créer un budget avec une période personnalisée.
7. Modifier puis archiver un budget ; vérifier sa présence dans Historique.
8. Restaurer le budget et vérifier que son suivi est conservé.
9. Refaire le parcours en anglais et en arabe RTL.

## Tests

- validation des montants, dates, périmètres et seuils ;
- états normal, proche et dépassé avec calcul exact ;
- parseur de montants en unité mineure ;
- convertisseurs Room des nouveaux enums ;
- déclaration de migration 3 vers 4 ;
- `assembleDebug` et suite `test` complète.
