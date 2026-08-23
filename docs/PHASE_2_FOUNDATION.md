# Phase 2 — Fondation technique Android

## Décisions de chaîne de compilation

- **AGP 8.13.2 / Gradle 8.13** : branche stable compatible avec API 36 et JDK 17, sans adopter prématurément la rupture AGP 9.
- **Kotlin 2.2.21 / KSP 2.2.21-2.0.4** : versions stables alignées ; KSP2 est utilisé pour Room et Hilt, sans KAPT.
- **Hilt 2.58** : dernière branche compatible avec AGP 8.x. Hilt 2.59+ impose AGP 9 ; cette migration coordonnée est volontairement reportée.
- **Compose BOM 2026.01.00** : BOM stable retenu afin de rester compatible avec `compileSdk 36`. Le BOM 2026.08.00 exigerait API 37 et AGP 9.1.1 au minimum ; cette migration sera évaluée séparément.
- **JDK 17** : niveau requis par AGP et utilisé pour les toolchains Java/Kotlin.
- **minSdk 26** : compromis entre couverture des appareils Android 8+ et coût de maintenance.
- **compileSdk/targetSdk 36** : API stable actuelle prise en charge par la chaîne choisie.

## Frontières de modules

La structure suit `docs/CONCEPTION.md`. En particulier, Room reste dans `:database` et DataStore dans `:data`, plutôt que de créer des variantes `:core:database` et `:core:datastore` contradictoires avec la conception validée.

Seules les features `onboarding` et `dashboard` contiennent une UI temporaire. Les autres modules de feature n'introduisent aucune classe placeholder : leur frontière Gradle est prête, leur code arrivera pendant la phase concernée.

## Choix de sécurité initiaux

- aucun accès réseau déclaré ;
- sauvegarde Android automatique désactivée ;
- domaines `database`, `sharedpref`, `file` et `root` exclus du transfert système ;
- secrets, fichiers de signature et configuration locale ignorés par Git ;
- variant release R8 prêt, sans signature privée.

## Validation attendue

```bash
./gradlew projects
./gradlew assembleDebug
./gradlew test
```

La CI exécute les mêmes validations avec JDK 17. La base Room elle-même sera créée en Phase 4 : le plugin Room, KSP, les dépendances et le répertoire de schémas sont déjà configurés, sans consommer prématurément une version de schéma vide.
