# Phase 21 — Favoris, médias et opérations rapides

## Médias privés

Les comptes, catégories, opérations et favoris peuvent référencer `media_assets`. Le sélecteur commun propose la galerie ou l'application photo via `FileProvider`, sans permission générale de stockage. L'utilisateur choisit de conserver les proportions ou d'appliquer un recadrage carré centré.

`LocalMediaRepository` décode l'orientation via `ImageDecoder` sur les API modernes, limite le grand côté à 1 600 px, compresse en WebP et produit une miniature carrée de 256 px. Les fichiers sont écrits par fichier temporaire puis renommés dans `files/media`, inaccessible aux autres applications. Les URI externes ne sont jamais conservées.

## Sauvegarde version 3

Le format chiffré `.bpp` version 3 encapsule une archive ZIP contenant `database.db` et les WebP privés, puis chiffre l'ensemble avec AES-GCM/PBKDF2. Les lecteurs v1/v2 restent supportés. Les entrées, chemins et tailles sont contrôlés. La restauration prépare les médias avant le remplacement atomique de la base et un coordinateur termine tout déplacement interrompu au prochain démarrage.

## Bibliothèque d'icônes

`BudgetIconCatalog` fournit des identifiants stables, issus des icônes Material libres utilisées par Budget++, répartis entre alimentation/boissons, maison/chantier, transport, santé, loisirs, travail, services et autres. La recherche utilise les descriptions localisées FR/EN/AR et chaque icône expose sa description aux technologies d'assistance.

## Favoris

Un favori contient nom, média ou icône/couleur, prix unitaire `Long`, compte, catégorie/sous-catégorie, description et comportement. La page Favoris est accessible depuis Opérations.

- **Immédiat** : le clic crée une dépense tout de suite.
- **Confirmation rapide** : quantité et prix restent modifiables avant validation.
- **Formulaire complet** : quantité, prix et date sont modifiables avant validation.

## Clics et idempotence

`favorite_click_batches` mémorise le dernier lot. Les clics du même favori distants de 1,5 seconde au maximum mettent à jour la même opération dans une transaction Room : quantité entière, prix unitaire et total exact par `Math.multiplyExact`. Au-delà, une nouvelle opération est créée. Les confirmations utilisent un UUID de requête déterministe : une répétition retourne l'opération existante.

Le Snackbar affiche immédiatement quantité et total et propose **Annuler**. L'annulation supprime logiquement l'opération et clôt le lot. Une modification manuelle ultérieure d'une opération détache proprement sa référence favorite et remet sa quantité à 1 afin d'éviter une incohérence prix×quantité.

## Room

Le schéma 11 et `MIGRATION_10_11` ajoutent `media_assets`, `favorites`, `favorite_click_batches`, les références média et les colonnes `favorite_id`, `unit_price_minor`, `quantity`. La migration est uniquement additive. Les lignes existantes obtiennent `quantity=1` et conservent tous leurs montants.

## Tests

Les tests couvrent multiplication exacte et débordement, fenêtre de clics, fusion dans une même opération, création séparée hors fenêtre, idempotence, annulation et soldes, récupération des médias staged, catalogue d'icônes, sauvegardes v1/v2/v3 et migration intégrale 1→11.
