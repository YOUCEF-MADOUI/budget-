# Phase 19 — Comptes et sous-comptes hiérarchiques

## Modèle

`accounts.parent_account_id` référence `accounts.id` avec suppression `RESTRICT`. La profondeur n'est pas limitée. `MIGRATION_8_9` ajoute la colonne nullable et l'index `(parent_account_id, display_order, deleted_at)` sans modifier les comptes existants, qui restent des racines.

Deux triggers SQLite refusent un parent identique au compte et parcourent récursivement les ancêtres avant chaque insertion ou déplacement. `AccountHierarchy.canMove` applique la même règle dans le domaine avant l'écriture. Cette double protection empêche les cycles, y compris en dehors du repository.

## Arbre et navigation

L'écran Comptes construit l'arbre de façon itérative, sans récursion Kotlin, et supporte donc les branches profondes. Chaque nœud peut être replié, ouvert ou utilisé comme parent d'un nouveau sous-compte. Le détail fournit un fil d'Ariane cliquable, les enfants directs et le déplacement vers une racine ou un autre parent autorisé.

Les comptes archivés sont affichés dans un arbre séparé. L'archivage porte sur tout le sous-arbre. La restauration réactive le sous-arbre et ses ancêtres afin qu'aucun compte actif ne reste caché derrière un parent archivé.

## Soldes propres et consolidés

Une CTE récursive Room calcule descendants, solde propre, solde consolidé, revenus/dépenses propres et consolidés. Les calculs restent des entiers SQLite/`Long`.

Pour un transfert :

- source dans le sous-arbre, destination hors sous-arbre : sortie consolidée ;
- destination dans le sous-arbre, source hors sous-arbre : entrée consolidée ;
- source et destination dans le même sous-arbre : impact consolidé nul.

Les opérations et transferts peuvent cibler tout compte actif, quel que soit son niveau.

## Modification, déplacement et suppression

Nom, type, icône, couleur, description et ordre restent modifiables sans toucher au solde ou aux opérations. Un déplacement ne change que `parent_account_id` et conserve toutes les données financières.

La suppression reste logique. Un compte avec opérations ou descendants exige une cible hors de son sous-arbre. Les opérations propres sont réaffectées, les enfants directs sont déplacés sous la cible et le solde initial est reporté, dans une transaction Room unique. Toute auto-référence de transfert annule l'ensemble. Toute règle récurrente, active ou en pause, bloque la suppression jusqu'à sa modification ou suppression.

## Sauvegarde et export

La sauvegarde chiffrée copie toute la base et conserve donc la hiérarchie. Les sauvegardes de schémas antérieurs migrent vers 10. L'export CSV des comptes inclut `parent_account_id`.

## Tests

- arbre de 1 000 niveaux sans dépassement de pile ;
- fil d'Ariane et aplatissement repliable ;
- rejet des cycles dans le domaine et par SQLite ;
- solde consolidé avec transfert interne net nul ;
- déplacement des descendants et report du solde initial ;
- protection des auto-transferts et récurrences ;
- migration intégrale 1→10 ;
- CI complète avec Lint, analyse statique, UI Compose et charges 10k/50k/100k.
