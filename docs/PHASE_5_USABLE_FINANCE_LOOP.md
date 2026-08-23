# Phase 5 — Domaine et boucle financière utilisable

## Objectif

Transformer le socle Room de la phase 4 en une expérience financière cohérente et réellement utilisable, tout en introduisant les règles de domaine prévues par la roadmap.

## Livré

### Navigation principale

- barre de navigation persistante pour Accueil, Comptes, Opérations et Catégories ;
- conservation de l'état de chaque destination avec Navigation Compose ;
- retour impossible vers l'écran technique de bienvenue après l'entrée dans l'application ;
- écran d'accueil recentré sur les actions financières disponibles.

### Comptes

- total des soldes actifs ;
- séparation claire entre comptes actifs et archivés ;
- création avec validation du nom et du solde initial ;
- état vide avec action directe ;
- confirmation avant archivage ou restauration ;
- message localisé en cas d'échec.

### Catégories

- filtres Dépense/Revenu et Actives/Archivées ;
- distinction entre catégories système traduites et catégories personnalisées ;
- création avec validation ;
- confirmation avant archivage ou restauration ;
- états vides et erreurs explicites.

### Dépenses, revenus, transferts et historique

- saisie unique adaptée au type d'opération ;
- compte destination distinct imposé pour un transfert ;
- catégorie compatible imposée pour une dépense ou un revenu ;
- validation du montant en unité mineure, sans virgule flottante ;
- limite localisée de la description ;
- filtres d'historique Toutes/Dépenses/Revenus/Transferts ;
- compteur de résultats et états vides filtrés ;
- confirmation destructive avant suppression logique ;
- recalcul automatique des soldes par les flux Room.

### Domaine

`FinanceValidator` centralise les invariants indépendamment d'Android : noms bornés, montants positifs, catégorie compatible, transfert vers un compte distinct et description bornée. Les repositories locaux l'utilisent avant toute écriture Room atomique.

## Internationalisation et accessibilité

Toutes les nouvelles chaînes sont fournies en français, anglais et arabe. Aucun texte d'interface n'est codé en dur. La navigation, les listes, les dialogues et les sélecteurs reposent sur les composants Compose directionnels et restent donc compatibles RTL. Les actions destructives ne reposent pas uniquement sur une couleur : elles possèdent un titre, un message et un libellé explicites.

## Base de données

Le schéma Room reste en version 1. Cette phase ne modifie aucune table et ne nécessite donc aucune migration.

## Vérification manuelle

1. Entrer dans l'application et utiliser les quatre destinations de la barre inférieure.
2. Créer un compte, vérifier son solde, puis confirmer son archivage et sa restauration.
3. Basculer entre catégories de dépense et de revenu, créer une catégorie, puis l'archiver.
4. Créer un revenu et une dépense ; vérifier les validations et les soldes.
5. Créer un deuxième compte et effectuer un transfert entre les deux.
6. Filtrer l'historique par type et supprimer une opération après confirmation.
7. Redémarrer l'application et vérifier la persistance des données.
8. Refaire le parcours en anglais puis en arabe et vérifier l'ordre RTL.

## Tests automatisés

- parseurs monétaires en unité mineure ;
- règles pures de `FinanceValidator` ;
- tests existants des convertisseurs Room et du Design System ;
- compilation de l'APK debug et tests unitaires de tous les modules via la CI.
