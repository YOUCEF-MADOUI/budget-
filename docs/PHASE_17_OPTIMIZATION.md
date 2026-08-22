# Phase 17 — Optimisation, performances et accessibilité

## Jeux de données et garde-fous

`PerformanceScaleTest` peuple la vraie base Room/SQLite jusqu'à 100 000 opérations et enregistre les jalons 10 000, 50 000 et 100 000. Le scénario exerce simultanément les triggers FTS, les index financiers, une page keyset, une recherche textuelle et un agrégat annuel. Les garde-fous CI sont de 180 secondes pour l'insertion complète et 5 secondes pour le groupe de requêtes indexées. Le test vérifie aussi le plan SQLite de la pagination date.

`MoneyFormatterPerformanceTest` formate 100 000 montants DZD avec un plafond de 5 secondes. `ReleaseQualityTest` bloque un APK debug dépassant 50 MiB.

## Room et pagination

Le schéma 7, via `MIGRATION_6_7`, ajoute sans supprimer les anciens index :

- `(deleted_at, occurred_at, id)` pour la pagination keyset chronologique ;
- `(deleted_at, amount_minor, id)` pour la pagination par montant ;
- `(deleted_at, local_date, type, category_id)` pour dashboard, budgets et analyses.

Le moteur exécute `PRAGMA optimize` à l'ouverture afin d'entretenir les statistiques du planificateur. La migration 1→7 reste couverte intégralement et ne transforme aucune donnée.

L'historique principal observe au maximum les 500 opérations les plus récentes ; la recherche avancée reste l'accès exhaustif aux 100 000 opérations. Sa fenêtre Compose en mémoire est plafonnée à 1 000 lignes tout en conservant le curseur keyset, évitant une croissance mémoire proportionnelle à la profondeur.

## Compose, démarrage et mémoire

Les listes longues utilisent `LazyColumn`/`LazyRow` et des clés stables. Les graphiques bornent leurs séries côté SQL. Les écrans principaux ne chargent plus l'historique complet. Le démarrage conserve les initialisations lourdes asynchrones (WorkManager) et Room reste paresseux ; `PRAGMA optimize` est l'unique travail synchrone lors de l'ouverture effective de la base.

Les formatteurs monétaires, auparavant reconstruits pour chaque montant, sont maintenant mis en cache par thread, locale et devise. La validation du code devise réutilise une expression régulière précompilée.

## Taille APK

Le build release conserve R8 et la réduction des ressources. Le test de taille empêche une régression au-delà de 50 MiB pour l'APK debug, plus volumineux que le release minifié. Aucun moteur de graphique ou bibliothèque de benchmark embarquée n'a été ajouté à l'application de production.

## Accessibilité, langues et RTL

L'audit automatisé vérifie la parité exacte de toutes les clés françaises, anglaises et arabes, ainsi que `supportsRtl`. Android Lint reste bloquant. Les titres de barres d'application portent maintenant la sémantique `heading`. La flèche retour utilise l'icône auto-miroir.

Les graphiques d'évolution inversent leur axe horizontal en RTL, tout en conservant leurs alternatives textuelles. Les cartes de budgets exposent prévu, dépensé, restant, pourcentage et état. Les montants masqués ont une description localisée. Les contrôles Material conservent leurs cibles tactiles minimales.

## CI et benchmarks

`assembleDebug` exécute Android Lint. `test` exécute l'analyse statique, les tests métier, Room/repositories, les tests Compose/locales/accessibilité, les tests de sécurité/sauvegarde et les charges 10k/50k/100k. La Phase 18 ne doit commencer qu'après succès des checks push et pull request.
