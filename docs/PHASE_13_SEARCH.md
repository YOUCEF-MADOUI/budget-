# Phase 13 — Recherche et filtres avancés

## Fonctionnalités

La destination Recherche avancée est accessible depuis l'historique des opérations. Elle combine : texte de description, type, compte source ou destination, catégorie, sous-catégorie, dates, montants minimum/maximum et quatre tris date/montant. Une action réinitialise immédiatement tous les critères.

Les filtres sont portés par `SearchViewModel` et sauvegardés dans `SavedStateHandle`. Ils survivent donc aux recompositions et à la recréation de la destination. La liste charge automatiquement les pages suivantes, affiche un indicateur progressif et possède des états initial, vide et erreur avec nouvelle tentative.

## Performance 100 000 opérations

La pagination est une pagination par clé (`keyset`) composée de la valeur de tri et de l'UUID. Elle n'utilise jamais `OFFSET`, dont le coût augmente avec la profondeur. Chaque page contient 50 résultats et la requête ne demande qu'une ligne supplémentaire pour déterminer l'existence de la page suivante.

La recherche de description repose sur une table virtuelle SQLite FTS4 `finance_transactions_fts` avec tokenisation Unicode et recherche par préfixe. Trois triggers maintiennent l'index après insertion, modification ou suppression physique. La suppression logique reste filtrée par la table financière source.

Les tris par montant utilisent le nouvel index `(amount_minor, occurred_at, deleted_at)`. Les filtres temporels, comptes, catégories et sous-catégories utilisent les index déjà présents. `TransactionSearchQueryFactory` ne génère que les clauses actives et conserve des paramètres liés, sans concaténer les données utilisateur.

## Room et migration

La base passe en version 6 avec `MIGRATION_5_6` : création et remplissage non destructif de l'index FTS, installation des triggers et ajout de l'index montant. Les opérations existantes sont immédiatement recherchables.

## Précision et validation

Les bornes monétaires sont converties et comparées en `Long`. `SearchFilterValidator` refuse les montants ou dates inversés et les dates ISO invalides. Aucun calcul financier n'utilise `Float` ou `Double`.

## Internationalisation et RTL

Tous les champs, tris, filtres et états existent en français, anglais et arabe. Les listes, puces et sélecteurs Compose sont directionnels et compatibles RTL.

## Tests

- combinaison des clauses SQL ;
- absence d'`OFFSET` et présence du curseur de pagination ;
- échappement des expressions FTS ;
- validation des bornes date/montant ;
- parseur monétaire ;
- déclaration de migration 5→6 ;
- `assembleDebug` et `test` complets.
