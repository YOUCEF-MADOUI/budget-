# Budget++

**Votre argent, plus clair. Votre budget, mieux maîtrisé.**

Budget++ est une application Android native de gestion budgétaire personnelle, conçue pour fonctionner hors ligne et protéger les données financières de l'utilisateur.

## État du projet

Le projet est actuellement en **phase 1 : conception et architecture générale**. Aucun code Android n'est encore engagé afin de faire valider les choix structurants avant l'initialisation technique.

- [Dossier complet de conception fonctionnelle et technique](docs/CONCEPTION.md)

## Principes

- Android natif : Kotlin, Jetpack Compose et Material 3
- Architecture Clean + MVVM pragmatique
- Room/SQLite comme source locale de vérité
- Montants financiers stockés en unités monétaires mineures (`Long`)
- Fonctionnement principal 100 % hors ligne
- Confidentialité par défaut et export uniquement à l'initiative de l'utilisateur
- Interface française en V1, prête pour l'anglais, l'arabe et le RTL

## Prochaine étape

Après validation de la conception : **phase 2 — projet Android et dépendances Gradle**, avec un premier build minimal compilable et testable.
