# Phase 9 — Catégories et sous-catégories avancées

## Fonctionnalités

- création et modification des catégories personnalisées ;
- hiérarchie catégorie → sous-catégories ;
- création et modification des sous-catégories ;
- sélection d'une icône et d'une couleur sémantique ;
- recherche sur les noms de catégories et sous-catégories ;
- filtres Dépense, Revenu et Archivées ;
- compteur d'opérations et de sous-catégories ;
- intégration des sous-catégories au formulaire d'opération ;
- archivage, restauration et suppression logique des catégories personnalisées ;
- protection en écriture des catégories système.

## Jeu système DZD

Le jeu local est complété pour les usages courants en Algérie : alimentation, transport, logement, santé, loisirs, factures et services, éducation, famille, vêtements, impôts et taxes, salaire, activité indépendante, retraite, allocations, cadeaux et autres revenus. Les identifiants restent stables et leurs libellés sont traduits en français, anglais et arabe.

## Intégrité et réaffectation

Les compteurs d'utilisation sont calculés par Room. Une catégorie ou sous-catégorie utilisée ne peut pas être archivée ou supprimée sans sélectionner une destination compatible :

- une catégorie de remplacement doit être distincte et du même type ;
- une sous-catégorie de remplacement doit appartenir à la même catégorie parente ;
- la réaffectation des opérations et l'archivage ou suppression logique sont exécutés dans la même transaction Room ;
- lors d'une réaffectation de catégorie, la sous-catégorie précédente est effacée afin d'éviter toute hiérarchie incohérente ;
- les catégories système ne sont ni modifiables, ni archivables, ni supprimables.

`CategoryReassignmentValidator` porte ces règles dans le domaine et possède des tests unitaires.

## Room et migration

Le schéma passe en version 3 avec `MIGRATION_2_3` :

- création de `subcategories` avec clé étrangère vers `categories` ;
- ajout nullable de `subcategory_id` à `finance_transactions` ;
- index sur la hiérarchie et les références d'opérations ;
- insertion non destructive des nouvelles catégories système sur une base existante.

Aucune opération existante n'est supprimée ou modifiée pendant la migration.

## Internationalisation et RTL

Tous les textes d'interface et catégories système existent en français, anglais et arabe. Les listes hiérarchiques utilisent des marges directionnelles Compose et respectent le RTL. Les icônes ont une description localisée et les couleurs ne remplacent jamais les libellés.

## Vérification manuelle

1. Rechercher et filtrer les catégories de dépense et de revenu.
2. Créer une catégorie en choisissant icône et couleur, puis la modifier.
3. Ajouter deux sous-catégories et utiliser l'une dans une nouvelle dépense.
4. Tenter d'archiver la sous-catégorie utilisée et vérifier que la réaffectation est exigée.
5. Tenter d'archiver ou supprimer une catégorie utilisée et choisir une catégorie compatible.
6. Vérifier que l'historique conserve toutes les opérations après réaffectation.
7. Vérifier qu'aucune action destructive n'est proposée sur les catégories système.
8. Refaire le parcours en anglais et en arabe.

## Tests

- validation des remplacements de catégorie et sous-catégorie ;
- déclaration de migration 2 vers 3 ;
- tests existants des montants, règles financières, dashboard et convertisseurs ;
- compilation `assembleDebug` et suite `test` complète.
