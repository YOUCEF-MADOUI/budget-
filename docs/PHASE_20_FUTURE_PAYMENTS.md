# Phase 20 — Échéances et paiements futurs

## Distinction métier

Une échéance représente une dépense envisagée mais non encore débitée. Contrairement à un budget, elle décrit un paiement précis. Contrairement à une récurrence, elle ne crée jamais automatiquement d'opération. Seule l'action explicite **Payer**, suivie d'une confirmation, produit une dépense réelle.

## Données

`future_payments` contient nom, description, montant prévu `Long`, date civile, catégorie/sous-catégorie, compte envisagé facultatif, priorité, rappel et notes. Son état est dérivé du montant effectivement payé, de la date et du drapeau d'annulation : à venir, aujourd'hui, en retard, partiellement payée, payée ou annulée.

`due_payments` relie chaque paiement à une opération financière. La requête de progression joint toujours l'opération non supprimée ; une modification ou suppression financière est donc reflétée. `due_reminder_events` garantit l'idempotence des notifications par échéance et date.

## Paiement atomique et idempotent

Le repository vérifie compte, date, montant positif et montant restant. Une transaction Room crée la dépense et le lien. L'identifiant de requête généré lors de l'ouverture du dialogue est la clé primaire du paiement et produit un identifiant d'opération déterministe. Un double appui avec la même requête retourne l'opération existante.

Les paiements partiels et multiples sont autorisés sans dépasser le restant. Annuler un paiement marque le lien annulé et supprime logiquement l'opération dans la même transaction, ce qui restaure automatiquement l'état et les soldes.

## Interface

La liste fournit compteurs à venir/en retard, total restant, filtres état, priorité, dates, compte et catégorie. Chaque carte propose paiement, historique, modification et annulation/restauration. Le formulaire gère rappel facultatif et jours d'anticipation. Le tableau de bord fournit un accès direct.

## Notifications

Un `CoroutineWorker` quotidien réclame uniquement les rappels arrivés à échéance et non déjà notifiés ce jour. Il vérifie l'autorisation Android 13 avant de réclamer, crée un canal local et ne déclenche aucun prélèvement. Les retards peuvent être rappelés une fois par jour jusqu'au paiement ou à l'annulation.

## Room

Le schéma 10 et `MIGRATION_9_10` ajoutent trois tables et leurs index, sans reconstruire ni supprimer les tables existantes. La sauvegarde chiffrée complète inclut automatiquement ces données et les anciennes sauvegardes migrent normalement.

## Tests

Les tests couvrent six états, retard, validation des montants, paiements partiels et multiples, idempotence, annulation et restauration du solde, rappels idempotents, DAO/repository, migration 1→11, Lint, analyse statique et charges 10k/50k/100k.
