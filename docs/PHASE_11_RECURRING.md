# Phase 11 — Opérations récurrentes

## Fonctionnalités

- dépenses, revenus et transferts récurrents ;
- fréquences quotidienne, hebdomadaire, mensuelle et annuelle ;
- début, fin facultative, fuseau et prochaine date civile ;
- activation, pause, modification et suppression logique ;
- sélection des comptes, catégories et sous-catégories ;
- historique des exécutions réussies et échouées ;
- rattrapage au démarrage et exécution différée avec WorkManager.

## Fiabilité et idempotence

Chaque occurrence est identifiée par la paire stable `(recurring_transaction_id, due_date)`, protégée par un index unique. L'identifiant de l'opération générée est également déterministe. La création de l'opération et de son occurrence `CREATED` est atomique dans Room. Une nouvelle tentative vérifie d'abord l'existence de l'occurrence et ne peut donc pas créer de doublon.

Une erreur est enregistrée comme occurrence `FAILED` avec un code non sensible. La prochaine date est ensuite avancée afin qu'une règle invalide ne bloque pas toutes les autres.

## WorkManager et rattrapage

`RecurringTransactionWorker` est un `CoroutineWorker` sans dépendance réseau. Il charge le repository via un point d'entrée Hilt, traite toutes les échéances dues, puis programme un travail unique pour la prochaine échéance. `BudgetPlusPlusApplication` demande un rattrapage immédiat à chaque ouverture. WorkManager conserve le travail après redémarrage du processus ou de l'appareil et applique sa stratégie de nouvelle tentative en cas d'échec transitoire.

## Dates, mois courts et fuseaux

Les échéances sont stockées en dates ISO locales avec le `ZoneId` IANA de création. L'instant de l'opération est résolu seulement lors de l'exécution avec ce fuseau. Les règles mensuelles et annuelles conservent le jour d'ancrage : un 31 est ramené au dernier jour d'un mois court puis revient au 31 le mois suivant. Le 29 février est ramené au 28 pendant une année non bissextile. `java.time` résout les intervalles et chevauchements des changements d'heure.

## Room et migration

La version 5 et `MIGRATION_4_5` ajoutent :

- `recurring_transactions` et ses références vers comptes/catégories ;
- `recurring_occurrences` ;
- index d'échéance et index unique d'idempotence.

Aucune donnée existante n'est modifiée.

## Internationalisation

L'interface complète existe en français, anglais et arabe RTL. Aucun texte d'interface n'est codé en dur.

## Tests

- mois courts et restauration du jour d'ancrage ;
- année bissextile ;
- résolution d'un changement d'heure ;
- identifiants idempotents ;
- parseur monétaire ;
- convertisseurs et migration Room 4→5 ;
- `assembleDebug` et `test` complets.
