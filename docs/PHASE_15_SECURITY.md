# Phase 15 — Sécurité de l'application

## Verrouillage et authentification

Le verrouillage local est facultatif et utilise un PIN de 4 à 8 chiffres. Budget++ ne stocke jamais le PIN : une clé HMAC-SHA256 non exportable est créée dans Android Keystore et signe le PIN avec un sel aléatoire de 256 bits. Seuls le sel et le vérificateur HMAC sont conservés dans DataStore. Les comparaisons utilisent `MessageDigest.isEqual`.

L'utilisateur peut activer BiometricPrompt lorsque l'appareil possède une biométrie compatible. Le bouton PIN reste toujours disponible. Une réussite biométrique réinitialise les tentatives échouées ; une annulation ou erreur revient simplement au PIN.

## Cycle de vie et délai

`SecurityGate` observe `ProcessLifecycleOwner`. À chaque passage en arrière-plan, l'instant est mémorisé. Au retour, le verrouillage est appliqué selon le délai choisi : immédiat, 30 secondes, 1, 2 ou 5 minutes. L'état initial est verrouillé dès le démarrage lorsqu'un PIN est actif.

## Limitation des tentatives

Les compteurs et la date de blocage sont persistés. Les deux premières erreurs n'imposent pas de délai, puis les délais progressent à 5 secondes, 30 secondes, 2 minutes et enfin 5 minutes. La comparaison du PIN n'est pas exécutée pendant une période de blocage.

## Protection visuelle et journaux

L'option Protection de l'écran applique `FLAG_SECURE`, empêchant les captures, l'enregistrement et l'aperçu lisible dans les applications récentes. Elle est activée par défaut et configurable.

Le code de sécurité ne journalise ni PIN, ni hash, ni clé, ni données financières. Les règles R8 de release suppriment les appels Android `Log` résiduels. Les erreurs exposées à l'interface sont génériques et localisées.

## PIN oublié

Le fonctionnement hors ligne et l'absence de secret récupérable rendent impossible la récupération du PIN. L'écran verrouillé présente donc une procédure en deux confirmations :

1. avertissement expliquant qu'une sauvegarde chiffrée est la seule possibilité de récupération des données ;
2. confirmation destructive détaillant comptes, opérations, budgets, catégories et paramètres supprimés.

Après confirmation, WorkManager est arrêté, la base est fermée et supprimée, DataStore est vidé et la clé Keystore est détruite. L'utilisateur doit relancer Budget++ ; une base neuve est alors créée. Aucune suppression n'a lieu avant la seconde confirmation.

## Internationalisation et RTL

Tous les écrans, délais, avertissements, erreurs et dialogues existent en français, anglais et arabe RTL. Aucun texte d'interface ou secret n'est codé en dur.

## Tests

- politique de format PIN ;
- progression des délais après échecs ;
- verrouillage selon le temps passé en arrière-plan ;
- suite existante de chiffrement de sauvegarde, intégrité et restauration atomique ;
- `assembleDebug` et `test` complets.
