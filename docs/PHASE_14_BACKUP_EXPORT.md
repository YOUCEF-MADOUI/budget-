# Phase 14 — Sauvegarde, restauration et export

## Sauvegarde chiffrée

Budget++ crée une copie cohérente de la base après un checkpoint WAL, puis la chiffre avant écriture dans le document choisi avec le sélecteur Android. Le format `.bpp` est versionné et contient sous chiffrement la version du schéma, la date de création, la taille, le SHA-256 et la base SQLite complète.

La clé AES-256 est dérivée exclusivement du mot de passe utilisateur avec PBKDF2-HMAC-SHA256, un sel aléatoire de 128 bits et 210 000 itérations. Chaque archive utilise un nonce GCM aléatoire de 96 bits. AES-GCM authentifie le contenu et les métadonnées de format. Aucune clé, aucun mot de passe et aucun secret par défaut n'est codé ou persisté.

## Compatibilité

Le lecteur accepte les formats d'archive 1 et 2. La version 1 historique utilise 120 000 itérations et l'authentification GCM ; la version 2 ajoute le SHA-256 interne et la date. Les bases SQLite de schémas 1 à 7 sont acceptées et les migrations Room normales sont appliquées au prochain démarrage.

## Aperçu et restauration atomique

Avant confirmation, l'archive est déchiffrée dans le cache privé puis contrôlée avec : en-tête, version, authentification GCM, SHA-256, taille bornée, signature SQLite, `PRAGMA integrity_check` et version du schéma. L'aperçu indique les nombres de comptes, opérations, catégories et budgets.

La restauration écrit et synchronise d'abord un fichier staged dans le dossier de la base. La base actuelle est déplacée vers un rollback, le staged est déplacé atomiquement, puis validé une seconde fois. Toute exception ou interruption restaure le rollback. Les fichiers WAL/SHM ne sont supprimés qu'après conservation du rollback. Un redémarrage de Budget++ recharge ensuite la base et ses éventuelles migrations.

## Export CSV

Le Storage Access Framework produit quatre exports indépendants UTF-8 : comptes, opérations, catégories et budgets. Les colonnes financières restent en unité mineure `Long`. Chaque champ est entouré de guillemets et les guillemets internes sont doublés conformément au CSV. Aucun accès général au stockage n'est demandé.

## Erreurs et confidentialité

Les fichiers invalides, altérés, trop volumineux, de version future ou ouverts avec un mauvais mot de passe sont refusés sans modifier les données. Les erreurs d'entrée/sortie et interruptions conservent la base courante. Les tableaux contenant les octets déchiffrés et mots de passe dérivés sont effacés dès que possible.

## Internationalisation

L'écran, les sélecteurs, confirmations, aperçus et erreurs sont disponibles en français, anglais et arabe RTL. Aucun texte d'interface n'est codé en dur.

## Tests

- aller-retour AES-GCM du format 2 ;
- lecture du format historique 1 ;
- refus d'un mot de passe incorrect ;
- refus d'un ciphertext modifié ;
- remplacement atomique réussi ;
- rollback après interruption de validation ;
- `assembleDebug` et `test` complets.
