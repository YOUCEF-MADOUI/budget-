# Phase 18 — Détail des comptes et réaffectation

## Détail complet

La route `accounts/{accountId}` affiche identité visuelle, description, type, solde, revenus, dépenses et nombre d'opérations. Les totaux sont calculés par une requête Room dédiée et restent en `Long`. L'historique utilise la recherche keyset par compte, une fenêtre mémoire bornée et des filtres texte, type, dates et tri date/montant.

Les actions rapides ouvrent l'éditeur d'opération avec le compte et le type Dépense, Revenu ou Transfert préremplis. Toucher une opération ouvre sa modification.

## Paramètres du compte

L'éditeur permet nom, type, icône, couleur, description et ordre d'affichage. `AccountDao.update` ne modifie jamais `initial_balance_minor` ni les opérations. Le solde est donc inchangé et reste dérivé de la même source financière.

Le schéma 8 ajoute seulement `accounts.description` avec une valeur vide par défaut via `MIGRATION_7_8`. Cette migration additive ne reconstruit aucune table et ne supprime aucune donnée.

## Déplacer une opération ou transférer de l'argent

**Transférer de l'argent** crée une opération `TRANSFER` et affecte les deux soldes.

**Déplacer une opération** conserve son identifiant, type, montant et contenu, mais remplace le rôle du compte sélectionné. Plusieurs UUID peuvent être réaffectés dans une transaction Room unique. Pour un transfert, le repository détermine si le compte est source ou destination et refuse toute transformation en auto-transfert.

La sélection, le compte cible et le nombre d'opérations sont présentés avant une confirmation explicite. Les soldes sont invalidés et recalculés seulement après commit.

## Suppression sécurisée

La suppression est toujours logique. Un compte avec opérations propose annulation, archivage ou compte de remplacement. Toutes les opérations sont réaffectées atomiquement avant `deleted_at`. Le solde initial est reporté sur le compte cible pour préserver le total financier.

Les règles récurrentes actives ou en pause protègent leur compte : la suppression échoue tant que la règle n'est pas supprimée ou modifiée. Une réaffectation créant un transfert source=destination est refusée et annule toute la transaction. Un compte vide peut être supprimé logiquement après confirmation.

## Qualité

Les interfaces et descriptions d'icônes/couleurs sont disponibles en français, anglais et arabe RTL. Les graphiques et flèches restent directionnels. Les montants sont des `Long`.

Les tests couvrent : modification des paramètres sans variation de solde, déplacement sélectionné, suppression avec report du solde initial, protection des auto-transferts, DAO réactifs, ViewModel et écran Compose. La migration complète 1→9, les charges 10k/50k/100k, Lint et l'analyse statique restent bloquants en CI.
