# Phase 22 — Finalisation et Budget++ 1.0

## Identité officielle

La planche fournie dans `assets/icon-board.png` est conservée comme source utilisateur. Seule la grande icône carrée de gauche a été extraite, aux coordonnées `(43, 15)` sur une zone `720 × 720`, sans les titres, consignes ni aperçus de la planche. Le résultat non redessiné est conservé dans `assets/phase22/icon-official-cropped.png`.

Les ressources Android sont organisées ainsi :

- icônes historiques 48, 72, 96, 144 et 192 px dans les dossiers `mipmap-mdpi` à `mipmap-xxxhdpi` ;
- icône adaptative à fond noir dans `mipmap-anydpi-v26` ;
- premier plan mis à l'échelle dans `drawable-nodpi/ic_launcher_foreground.png`, avec une marge adaptée aux masques constructeur ;
- variante monochrome extraite des formes de l'icône officielle dans `drawable-nodpi/ic_launcher_monochrome.png` ;
- déclaration monochrome Android 13+ dans `mipmap-anydpi-v33` ;
- références `android:icon` et `android:roundIcon` explicites dans le manifeste.

L'écran d'accueil affiche également cette identité officielle. Les formulations techniques temporaires et l'accès utilisateur au catalogue de développement ont été retirés. L'écran est défilable afin de rester utilisable avec un petit écran ou une grande taille de police.

## Version

Budget++ porte désormais :

- `versionName = "1.0.0"` ;
- `versionCode = 10000`.

Le code encode la version majeure de manière monotone et laisse de la place aux versions 1.0.x et 1.x futures.

## Données et compatibilité

Aucune table ni colonne n'est modifiée en Phase 22. La base reste en schéma Room 11 et la chaîne non destructive 1→11 est conservée. Aucun appel à `fallbackToDestructiveMigration` n'est autorisé.

La couverture existante contrôle notamment :

- migrations additives 1→11 et conservation des lignes historiques ;
- hiérarchies de comptes, cycles et profondeur de 1 000 niveaux ;
- opérations, transferts, modifications et réaffectations atomiques ;
- budgets, échéances, paiements partiels et annulations ;
- récurrences et occurrences idempotentes ;
- favoris, quantités exactes, fenêtre de clics, annulation et absence de doublons ;
- sauvegardes chiffrées v1/v2/v3, médias privés et restauration interrompue ;
- recherche keyset/FTS et charges 10 000, 50 000 et 100 000 opérations ;
- calculs métier monétaires en `Long` (les `Float` restent limités au rendu graphique et au redimensionnement d'images).

## Commandes de validation

Depuis la racine, avec JDK 17 et Android SDK 36 :

```bash
./gradlew projects --stacktrace
./gradlew clean assembleDebug --stacktrace
./gradlew lint --stacktrace
./gradlew staticAnalysis --stacktrace
./gradlew test --stacktrace
```

`assembleDebug` dépend aussi de `bundleRelease` pour vérifier systématiquement la génération du bundle 1.0. Android Lint reste une dépendance d'assemblage via les convention plugins, et les tests dépendent de l'analyse statique.

Pour installer et lancer sur un appareil ou émulateur API 26+ :

```bash
./gradlew installDebug
adb shell am force-stop com.budgetplusplus.app.debug
adb shell monkey -p com.budgetplusplus.app.debug -c android.intent.category.LAUNCHER 1
```

Vérifications manuelles minimales : création d'un compte parent et enfant, dépense/revenu/transfert, modification, budget, échéance avec deux paiements, récurrence, favori à clics rapides, image privée, recherche, sauvegarde/restauration, verrouillage PIN et biométrie. Répéter en FR/EN/AR, RTL arabe, clair/sombre, petit écran, tablette et grandes polices.

## Sorties de construction

Après une construction réussie :

- APK de test installable : `app/build/outputs/apk/debug/app-debug.apk` ;
- AAB release non signé : `app/build/outputs/bundle/release/app-release.aab` ;
- rapports Lint : `app/build/reports/lint-results-debug.html` et rapports équivalents des modules ;
- rapports de tests : `*/build/reports/tests/`.

Les répertoires `build/` sont volontairement ignorés par Git. Les binaires doivent être régénérés depuis le commit publié, et non versionnés dans le dépôt.

## Signature sécurisée

Aucun keystore ni mot de passe n'est fourni ou enregistré dans Git. Pour la publication :

1. conserver le keystore de production dans un coffre externe sauvegardé ;
2. utiliser **Build > Generate Signed Bundle / APK** dans Android Studio ;
3. sélectionner `app`, le variant `release` et le keystore par son chemin externe ;
4. saisir les secrets uniquement dans l'interface sécurisée ou le gestionnaire de secrets CI ;
5. ne jamais cocher une option qui écrit ces secrets dans le projet ;
6. vérifier le certificat avec `jarsigner -verify -verbose -certs app-release.aab` ;
7. conserver séparément l'empreinte du certificat et tester l'AAB via Play Internal Testing.

Le fichier `.gitignore` exclut les formats usuels de clés et les propriétés locales de signature. Une clé de publication réelle et la validation Play Console restent nécessairement des validations humaines.

## Critères de sortie

La version 1.0 est prête techniquement lorsque les tâches ci-dessus sont vertes et que l'APK a été installé/lancé sur au moins un émulateur et un appareil physique. La signature finale, le test biométrique matériel, l'aspect exact des masques de lanceurs constructeurs et le parcours Play Internal Testing nécessitent une validation humaine.
