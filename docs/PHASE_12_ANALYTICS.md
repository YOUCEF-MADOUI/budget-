# Phase 12 — Statistiques et analyses financières

## Fonctionnalités

- revenus, dépenses et solde net de la période ;
- évolution chronologique quotidienne ou mensuelle ;
- comparaison en montant et pourcentage avec la période précédente de même durée ;
- moyennes quotidiennes et mensuelles des revenus et dépenses ;
- répartition et classement des huit catégories les plus dépensières ;
- filtres semaine, mois, année et période personnalisée ;
- états de chargement, erreur et absence de données ;
- destination Analyse dans la navigation principale.

## Requêtes Room

`AnalyticsDao` exécute trois requêtes observables optimisées : agrégats de période, évolution groupée et dépenses par catégorie. Les bornes utilisent `local_date`, dont l'index composite existe déjà. Les périodes longues sont groupées par mois directement dans SQLite afin de limiter le volume transféré à l'interface. La comparaison précédente est observée en parallèle et toute modification d'opération actualise automatiquement l'analyse.

## Précision

Tous les agrégats, écarts et moyennes restent en `Long` en unité mineure. Les moyennes utilisent `BigDecimal` avec arrondi explicite `HALF_UP`. Les pourcentages de comparaison utilisent également `BigDecimal`. Les conversions en `Float` sont strictement réservées aux coordonnées et barres visuelles des graphiques.

## Accessibilité et RTL

Le graphique d'évolution possède une description sémantique localisée et ses trois séries sont également disponibles sous forme de cartes et métriques textuelles. Le classement des catégories contient rang, nom et montant. Les composants directionnels Compose et la navigation sont compatibles arabe RTL.

## Tests

- arrondi exact des moyennes ;
- nombre de jours inclusifs ;
- moyenne mensuelle sur un changement d'année ;
- tests existants Room, migrations, montants, budgets et récurrences ;
- `assembleDebug` et `test` complets.

## Vérification manuelle

1. Ouvrir Analyse depuis la barre principale.
2. Comparer semaine, mois et année avec l'historique des opérations.
3. Vérifier les écarts avec la période précédente.
4. Choisir une période personnalisée.
5. Vérifier évolution, moyennes et classement des catégories.
6. Ajouter puis supprimer une dépense et constater la mise à jour immédiate.
7. Vérifier les états vides sur une installation neuve.
8. Refaire le parcours en anglais et en arabe RTL.
