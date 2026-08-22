# Phase 17 — Modification des opérations et création rapide de catégories

## Modification d'une opération

Une opération peut être ouverte depuis l'historique, les résultats de recherche ou le détail d'un compte. La route `transactions/edit/{transactionId}` recharge toujours l'opération depuis Room à partir de son identifiant ; aucun objet financier complet ne transite dans la navigation.

L'éditeur permet de changer type, montant, date civile, compte source, compte destination, catégorie, sous-catégorie et description. Avant l'écriture, une confirmation explique que soldes et statistiques seront recalculés.

`LocalTransactionRepository.update` exécute dans une transaction Room : chargement de l'entité existante, validation métier, validation des comptes et de la hiérarchie, résolution de la date dans le fuseau conservé, puis `@Update`. L'identifiant, la date de création, la devise et le fuseau sont conservés. Les `Flow` des comptes, budgets, dashboard et analyses sont invalidés ensemble à la fin de la transaction et recalculent les anciens et nouveaux comptes.

Une occurrence générée par une récurrence reste une opération ordinaire modifiable. La table `recurring_transactions` n'est jamais écrite par l'éditeur ; la règle d'origine et ses prochaines échéances restent donc inchangées.

## Cohérence métier

- dépense/revenu : destination absente et catégorie du même type obligatoire ;
- transfert : destination distincte, catégorie et sous-catégorie absentes ;
- sous-catégorie : doit appartenir à la catégorie choisie ;
- montant : positif, stocké en `Long` ;
- date : ISO valide, avec résolution DST par `java.time` ;
- mise à jour atomique et identifiant stable.

## Création rapide de catégorie

Depuis l'éditeur d'une dépense ou d'un revenu, **Nouvelle catégorie** ouvre un dialogue superposé sans quitter le formulaire. Le nom, l'icône, la couleur et une sous-catégorie facultative sont créés via le repository. Les nouvelles méthodes retournent leurs identifiants ; catégorie et sous-catégorie sont donc sélectionnées immédiatement. Les états `rememberSaveable` conservent montant, date, comptes et description saisis auparavant.

Le type de la catégorie est imposé par le type d'opération courant, évitant toute incohérence revenu/dépense. Les icônes ont une description localisée et chaque pastille de couleur expose également un libellé accessible.

## Navigation

- historique : dialogue avec actions Modifier, Supprimer et Fermer ;
- recherche avancée : toucher une ligne ouvre l'éditeur ;
- comptes : toucher un compte actif ouvre son détail et ses opérations ;
- création : le FAB et l'état vide ouvrent désormais l'éditeur plein écran.

## Données et migrations

Aucune table n'est reconstruite et aucune migration destructive n'est utilisée. La mise à jour conserve toutes les données et s'appuie sur le schéma 7 additif de l'optimisation.

## Tests

- DAO : changement dépense→revenu, montant et compte, avec soldes exacts ;
- repository : transaction atomique, même identifiant, nouveau type/compte/montant ;
- domaine : validations existantes des catégories et transferts ;
- UI Compose : historique et cartes accessibles ;
- suite complète Room, migrations 1→7, budgets, récurrences, sauvegarde, sécurité, locales et RTL.
