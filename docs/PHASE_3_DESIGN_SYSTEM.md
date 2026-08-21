# Phase 3 — Design System Budget++

## Objectif et identité

Le Design System traduit les principes de Budget++ — confiance, clarté, maîtrise et confidentialité — en composants Compose réutilisables. L'identité évite les métaphores bancaires surchargées : surfaces calmes, vert profond distinctif, bleu d'information, arrondis mesurés et hiérarchie typographique forte pour les montants.

Le monogramme définitif reste hors périmètre. Les icônes Material sont centralisées dans `BudgetIcons` afin de pouvoir être remplacées par une famille originale sans modifier les features.

## Organisation

```text
core/designsystem/src/main/kotlin/com/budgetplusplus/core/designsystem/
├── theme/       Color, Theme, Type, Shape
├── tokens/      dimensions, espacements, rayons, élévations
├── components/  contrôles et états génériques
├── financial/   composants spécialisés et règles visuelles
├── icons/       point d'entrée temporaire des icônes
├── util/        formatage monétaire sans virgule flottante
├── demo/        catalogue interne navigable
└── previews/    previews clair, sombre, RTL et grand texte
```

`core:designsystem` ne dépend d'aucune feature. Les composants acceptent des chaînes déjà localisées et des callbacks ; ils ne connaissent ni Room, ni repositories, ni règles métier de feature.

## Palette

### Couleurs Material sémantiques

Les thèmes clair et sombre définissent `primary`, `onPrimary`, conteneurs primaires, `secondary`, `tertiary`, arrière-plan, surfaces et niveaux de conteneur, variantes, contours et erreur. Les primitives (`BudgetGreen*`, `Slate*`, `FinanceBlue*`) restent internes : les écrans consomment exclusivement `MaterialTheme.colorScheme`.

- Primaire clair : vert `#176B5B` ; primaire sombre : `#72D6BE`.
- Tertiaire clair : bleu `#365E8D` ; tertiaire sombre : `#A5C8F8`.
- Fonds : `#F8FCF9` en clair et `#171D1A` en sombre.
- Erreur : rouge Material accessible `#B3261E` / `#FFB4AB`.

### Couleurs financières

`MaterialTheme.financialColors` expose des tokens sémantiques et leurs conteneurs :

- `income` : revenu positif ;
- `expense` : dépense ou solde négatif ;
- `transfer` : mouvement neutre entre comptes ;
- `savings` : épargne ;
- `warning` : attention ;
- `budgetNearLimit` et `budgetExceeded` ;
- `disabled` ;
- `chartSeries` : cinq séries différenciées.

Une couleur est toujours accompagnée d'un signe, d'un montant, d'un pourcentage, d'un libellé d'état ou d'une description d'accessibilité.

## Typographie

La police système sans serif garantit couverture arabe, performance et respect des réglages Android. L'échelle Material est redéfinie avec poids et hauteurs de ligne cohérents.

Styles financiers supplémentaires :

- `BalanceDisplayStyle` : 40 sp / 48 sp, gras ;
- `LargeAmountStyle` : 28 sp / 36 sp ;
- `CompactAmountStyle` : 16 sp / 24 sp, semi-gras ;
- `PercentageStyle` : 14 sp / 20 sp, gras.

Aucun composant financier ne fixe une seule ligne pour une information essentielle. Les grandes tailles de texte sont représentées par des previews à `fontScale = 1.6`.

## Espacement, formes et dimensions

Les valeurs sont centralisées dans :

- `BudgetSpacing` : 0, 4, 8, 12, 16, 24, 32 et 48 dp ;
- `BudgetRadii` : 4, 8, 12, 20, 28 et rayon complet ;
- `BudgetSizes` : cible tactile 48 dp, boutons 52/56 dp, champs 56 dp, icônes 16/24/32 dp, FAB 56 dp ;
- `BudgetElevation` : 0, 1, 3 et 6 dp.

Les cartes utilisent une élévation légère et un contour sémantique. Les formes Material dérivent de ces tokens.

## Composants génériques

### Actions

- `BudgetPrimaryButton`, avec état chargement, désactivé et icône facultative ;
- `BudgetSecondaryButton` ;
- `BudgetTextButton` ;
- `BudgetFullWidthPrimaryButton` ;
- `BudgetAddFab`, compact ou étendu.

Les états pressé/focus/sélectionné sont fournis par Material 3 ; le chargement expose un `stateDescription` et bloque les doubles actions.

### Saisie et sélection

- `BudgetTextField` ;
- `BudgetAmountField`, qui conserve une chaîne de saisie et ne produit jamais de `Double` métier ;
- `BudgetSelector` ;
- `BudgetSearchBar` avec action d'effacement accessible.

Le parsing vers les unités mineures appartiendra aux use cases/formulaires de transaction, pas au Design System.

### Conteneurs et listes

- `BudgetCard`, `BudgetClickableCard`, `BudgetSummaryCard` ;
- `BudgetListRow` ;
- `BudgetCategoryIcon` ;
- `BudgetDivider`, `BudgetBadge`, `BudgetFilterChip`, `BudgetAssistChip`.

### Navigation et retour utilisateur

- `BudgetTopAppBar` ;
- `BudgetBottomNavigation` ;
- `BudgetLoadingIndicator` ;
- `BudgetConfirmationDialog` ;
- `BudgetSnackbarHost` ;
- `BudgetEmptyState`, `BudgetErrorState`, `BudgetOfflineState` ;
- `BudgetSkeleton` statique, sans animation imposée.

## Composants financiers

### `MoneyText`

Reçoit un `Long` en unité mineure, un code ISO et un `AmountTone`. `MoneyFormatter` utilise `BigDecimal` et `NumberFormat`, jamais `Float`/`Double` pour le montant. Il prend en charge les signes, les montants masqués, DZD avec symbole DA, EUR, USD et les règles de groupement de la locale. Une description lecteur d'écran annonce revenu, dépense, transfert ou montant neutre.

### `BalanceCard`

Titre, montant, devise, variation, masquage et état positif/négatif/neutre. L'état est écrit dans un badge en plus de la couleur.

### `IncomeExpenseCard`

Présente revenus, dépenses et différence pour une période. Les signes et tons restent explicites.

### `TransactionRow`

Icône de catégorie, catégorie, description, date, compte, montant, type et indicateur de récurrence. Le texte principal n'est pas tronqué volontairement.

### `FinancialBudgetCard`

Prévu, dépensé, restant, pourcentage, progression et état textuel. Les règles visuelles sont : normal sous 90 %, proche de la limite à partir de 90 %, dépassé strictement au-dessus de 100 %. Le seuil est paramétrable.

### `AccountCard`

Nom, type, montant, devise, icône et états actif/masqué/archivé. Le masquage remplace le montant et sa sémantique ; l'archivage combine texte et atténuation.

## Accessibilité

- cible tactile minimale de 48 dp ;
- contrastes portés par les color schemes Material et revus pour chaque token financier ;
- descriptions sur toutes les icônes interactives ;
- icônes décoratives sans description ;
- descriptions financières contextualisées ;
- état budgétaire et état de compte toujours textuels ;
- ordre de composition identique à l'ordre de lecture ;
- aucune limite de longueur supposée pour les textes localisés ;
- montants importants autorisés à revenir à la ligne ;
- squelette et chargement annoncés ;
- respect du thème et du facteur de police système.

## Internationalisation et RTL

Les ressources existent dans `values`, `values-fr`, `values-en` et `values-ar`. Les layouts emploient les notions start/end implicites de Compose et aucun positionnement absolu gauche/droite. `NumberFormat` applique la locale active. Une preview arabe force le RTL et une preview grand texte vérifie les composants les plus denses.

## Previews

`DesignSystemPreviews.kt` couvre :

- thème clair et sombre ;
- solde positif et négatif ;
- arabe/RTL ;
- facteur de police 1,6 ;
- budgets normal, proche de la limite et dépassé ;
- ligne d'opération récurrente ;
- états vide et erreur ;
- montants DZD, EUR, USD et masqué.

## Écran interne de démonstration

`DesignSystemDemoScreen` est accessible depuis l'écran technique de Phase 2. Il expose palette, typographie, boutons, états de chargement, champs, sélecteurs, cartes financières, navigation, dialogue, snackbar, états vide/erreur et squelette. Son bouton permet de forcer clair/sombre. Toutes les données sont fictives et cet écran ne fait pas partie de la navigation utilisateur définitive.

## Tests

- `MoneyFormatterTest` : DZD/DA, unités mineures, signes, zéro, négatif, très grande valeur, USD et validation de code ;
- `BudgetVisualStateTest` : 75/90/100/>100 %, plan nul, valeur négative et `Long.MAX_VALUE`.

Les calculs de progression convertissent uniquement un ratio final en `Float`, format requis par Compose ; les montants restent en `Long`/`BigDecimal`.

## Utilisation dans une feature

```kotlin
BudgetPlusPlusTheme {
    BalanceCard(
        title = stringResource(R.string.balance_total),
        balanceMinor = uiState.balanceMinor,
        currencyCode = uiState.currencyCode,
    )
}
```

Règles :

1. utiliser les tokens sémantiques, jamais une couleur primitive ;
2. transmettre les textes depuis les ressources de la feature ;
3. transmettre des unités mineures `Long` à `MoneyText` ;
4. fournir une description aux icônes interactives et `null` aux décoratives ;
5. ne pas recréer localement bouton, carte, état vide ou formateur monétaire ;
6. ajouter une preview claire/sombre ou accessibilité lorsqu'une nouvelle variante structurelle apparaît.

## Limites de Phase 3

- logo et icônes originaux définitifs non créés ;
- aucune donnée réelle, ViewModel ou logique financière métier ;
- graphiques réels reportés aux phases Dashboard/Statistiques ; seule la palette de séries est définie ;
- comportement de thème persisté dans DataStore reporté à la phase Paramètres ;
- tests instrumentés TalkBack à compléter avec les écrans métier.
