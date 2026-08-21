# Phase 4 — Base Room et premières fonctionnalités

## Livré

Cette itération rend la première boucle financière utilisable hors ligne :

- base Room `BudgetPlusDatabase` version 1, schéma exporté par KSP ;
- espace personnel local initialisé automatiquement en DZD ;
- tables `workspaces`, `accounts`, `categories` et `finance_transactions` avec clés étrangères et index ;
- DAO réactifs basés sur `Flow`, solde de compte calculé depuis les opérations ;
- repositories locaux injectés par Hilt ;
- création et archivage/restauration de comptes ;
- catégories système traduisibles et catégories personnalisées ;
- saisie de dépenses, revenus et transferts, historique et suppression logique ;
- montants stockés exclusivement en `Long` (unité mineure), sans `Float` ni `Double` ;
- interfaces et catégories système disponibles en français, anglais et arabe ; mise en page RTL gérée par Compose et `supportsRtl`.

## Fichiers structurants

- `database/.../BudgetPlusDatabase.kt`
- `database/.../entity/Entities.kt`
- `database/.../dao/*Dao.kt`
- `database/.../di/DatabaseModule.kt`
- `domain/.../repository/FinanceRepositories.kt`
- `data/.../repository/LocalFinanceRepositories.kt`
- `feature/accounts/...`
- `feature/categories/...`
- `feature/transactions/...`

## Règles et intégrité

Une opération possède un montant strictement positif. Une dépense ou un revenu exige une catégorie du même type. Un transfert exige un compte destination distinct et ne possède pas de catégorie. L'écriture est exécutée dans une transaction Room. Les suppressions d'opérations sont logiques afin de préserver l'audit.

La version 1 est le schéma initial : aucune migration n'est nécessaire. Toute évolution ultérieure devra augmenter la version, fournir une migration explicite et conserver les schémas JSON exportés.

## Vérification manuelle

1. Ouvrir l'application puis **Commencer**.
2. Créer deux comptes et vérifier leur solde initial.
3. Consulter les catégories préinstallées et créer une catégorie personnalisée.
4. Ajouter un revenu, une dépense et un transfert.
5. Vérifier que les soldes changent correctement et que les données restent après redémarrage.
6. Basculer la langue Android en anglais puis en arabe ; vérifier les libellés et le sens RTL.
7. Appuyer sur une opération pour la supprimer logiquement, ou sur un compte pour l'archiver/restaurer.
