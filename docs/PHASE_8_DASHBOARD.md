# Phase 8 — Tableau de bord financier

## Objectif

Fournir une vue financière complète, réactive et hors ligne à partir de la source de vérité Room, sans recalcul approximatif des montants.

## Fonctionnalités

- solde total courant et soldes détaillés par compte actif ;
- revenus, dépenses et solde net de la période ;
- courbe d'évolution cumulée du solde ;
- répartition des dépenses par catégorie avec montants, pourcentages et barres accessibles ;
- cinq opérations récentes pour la période ;
- filtres semaine, mois, année et période personnalisée ;
- sélection personnalisée par calendrier avec bornes inclusives ;
- états de chargement, erreur, absence de compte, absence d'opération et absence de dépense catégorisée ;
- accès direct aux comptes et à l'historique complet.

## Architecture réactive

`DashboardViewModel` transforme le filtre sélectionné en bornes civiles dans le fuseau local. `DashboardRepository` expose un `Flow<DashboardData>`. Un changement de période utilise `flatMapLatest`, annule les anciennes observations puis s'abonne aux nouvelles requêtes.

`DashboardDao` exécute directement dans SQLite :

- les sommes de revenus et dépenses ;
- le solde d'ouverture ;
- les flux quotidiens agrégés ;
- les dépenses groupées par catégorie ;
- les opérations récentes limitées.

Les soldes quotidiens sont cumulés par `BalanceEvolutionCalculator` avec des `Long` en unité mineure et les opérations arithmétiques exactes de Java. Les conversions en `Float` sont limitées aux coordonnées visuelles des graphiques et ne servent jamais aux calculs financiers.

## Optimisation et migration

Le schéma Room passe de la version 1 à la version 2. `MIGRATION_1_2` ajoute deux index adaptés aux agrégats du tableau de bord :

- `(occurred_at, type, deleted_at)` ;
- `(category_id, occurred_at, deleted_at)`.

Aucune donnée n'est réécrite ou supprimée. Les requêtes restent observables et sont invalidées automatiquement par Room après chaque création, modification ou suppression logique d'opération.

## Internationalisation et accessibilité

Tous les textes sont disponibles en français, anglais et arabe. Les composants Compose respectent automatiquement le RTL. La courbe fournit une description sémantique et les graphiques de catégories possèdent toujours une alternative textuelle comprenant nom, montant et pourcentage ; la couleur n'est jamais l'unique information.

## Vérification manuelle

1. Créer deux comptes puis plusieurs revenus, dépenses et transferts.
2. Ouvrir l'accueil et vérifier le total et chaque solde de compte.
3. Comparer les agrégats aux opérations de la période.
4. Basculer entre semaine, mois et année.
5. Sélectionner une période personnalisée dans le calendrier.
6. Vérifier la courbe, les catégories et les cinq opérations récentes.
7. Ajouter puis supprimer une opération et constater la mise à jour immédiate.
8. Vérifier les états vides avec une nouvelle installation.
9. Refaire le parcours en anglais et en arabe, notamment le calendrier et les listes RTL.

## Tests

- bornes semaine/mois/année ;
- évolution cumulée exacte en unité mineure ;
- déclaration de la migration 1 vers 2 ;
- tests existants des montants, règles métier et convertisseurs Room ;
- `assembleDebug` et `test` sur l'ensemble du projet.
