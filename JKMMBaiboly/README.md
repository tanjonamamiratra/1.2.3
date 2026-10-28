# Baiboly JKMM — application Android

Bible malgache (Dikanteny 1865) pour l'église JKMM. Kotlin + Jetpack Compose, lecture hors connexion.

## 0. Obtenir l'APK (fichier à installer sur un téléphone)
**Option A : Android Studio** (voir section 1) puis `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
Le fichier est créé dans `app/build/outputs/apk/debug/app-debug.apk`. Envoyez-le sur le téléphone (WhatsApp, câble USB…) et ouvrez-le ; autorisez l'installation depuis cette source si Android le demande.

**Option B : sans rien installer, avec GitHub (gratuit)**
1. Créez un compte sur https://github.com puis un nouveau dépôt **privé**.
2. Envoyez-y tout le contenu de ce dossier (bouton `Add file > Upload files`, y compris le dossier `.github`).
3. Onglet **Actions > Construire l'APK > Run workflow**. Attendez 5 à 10 minutes.
4. Ouvrez l'exécution terminée et téléchargez **Baiboly-JKMM-apk** : le zip contient `app-debug.apk`.

Cet APK de test sert à essayer l'application. Pour le Play Store, il faut un fichier signé (AAB, voir section 4).

## 1. Ouvrir et lancer le projet
1. Installez **Android Studio** (version récente, gratuite) : https://developer.android.com/studio
2. `File > Open` et choisissez ce dossier. Attendez la fin de « Gradle Sync » (internet nécessaire la première fois).
3. Branchez un téléphone Android (mode développeur + débogage USB) ou créez un émulateur, puis cliquez sur ▶ **Run**.

## 2. Ce que contient l'application
- **Écran de chargement animé** : le logo apparaît en rebondissant puis flotte, le titre se révèle et une barre dorée se remplit (3 secondes ; réglable avec `SPLASH_MILLIS` dans `ui/SplashScreen.kt`)
- **Personnalisation (Safidy)** : aperçu en direct, mode clair/sombre/automatique, 5 couleurs (Bordeaux, Volamena, Maitso, Manga, Volomparasy), 4 fonds (Klasika avec branches de laurier, Malefaka, Maraina, Tsotra), taille et police du texte (Serif/Sans), interligne, texte justifié. Les choix sont enregistrés sur le téléphone
- **Barre de navigation en bas** : Fandraisana (accueil), Baiboly, Karohy (recherche), Safidy (réglages)
- **Accueil** : verset du jour (« Andininy anio ») avec Vakio / Adikao, puis l'historique « Vakiana vao haingana » (8 derniers chapitres lus, enregistrés sur le téléphone)
- **Baiboly** : Testamenta Taloha / Vaovao, filtre « Hitady boky… », puis un livre ouvre la grille des **toko** et, ensuite, des **andininy** : on peut choisir un verset ou une plage (ex. 1–5), ou lire le toko entier. Les andininy choisis sont mis en évidence et le texte défile jusqu'au premier
- **Lecture** : plein écran, glissement d'un chapitre à l'autre, barre flottante (‹ chapitre ›), bouton « Aa » (taille du texte et thème)
- **Versets** : toucher pour sélectionner, puis Adikao (copier), Zarao (partager) ou Hasongadina (surligner, enregistré)
- **Recherche** dans toute la Bible avec mots surlignés ; on peut aussi taper une référence (« Jaona 3:1-5 ») pour y aller directement, mode clair / sombre / automatique
- Mise à jour du contenu à distance (voir ci-dessous)

Les textes de l'interface sont en malgache ; corrigez-les dans les fichiers `ui/*.kt` si une formulation ne convient pas.

## 3. Mettre à jour le CONTENU à distance (sans nouvel APK)
L'application vérifie une fois par jour (et via Safidy > « Hamarino ny fanavaozana ») si une base plus récente existe.
Le plus simple : **GitHub Pages** (gratuit, tout se fait dans le navigateur).

**Mise en place (une seule fois)**
1. Sur github.com, créez un dépôt **public** nommé `baiboly-contenu` (le texte de 1865 est du domaine public).
2. Envoyez-y le contenu du dossier `baiboly-contenu` fourni (`manifest.json`, `baiboly_mg1865_v1.db`, `index.html`) avec `Add file > Upload files`.
3. `Settings > Pages` : Source = `Deploy from a branch`, Branch = `main`, dossier `/ (root)`, puis Save. Après 1 à 2 minutes, l'adresse est `https://VOTRE-NOM.github.io/baiboly-contenu/`.
4. Vérifiez dans un navigateur que `https://VOTRE-NOM.github.io/baiboly-contenu/manifest.json` s'affiche.
5. Dans `data/ContentUpdater.kt`, remplacez `BASE_URL` par `https://VOTRE-NOM.github.io/baiboly-contenu/` (avec le `/` final), puis recompilez l'APK **une fois**.

**À chaque correction**
1. Corrigez la base SQLite (ou envoyez la liste des corrections pour qu'elle soit appliquée).
2. `python tools/preparer_mise_a_jour.py ma_base_corrigee.db 2` (le 2 = nouveau numéro de version, à augmenter à chaque fois).
3. Dans le dépôt `baiboly-contenu`, envoyez les deux fichiers du dossier `publication/` : le nouveau `baiboly_mg1865_v2.db` et `manifest.json` (qui remplace l'ancien).
4. Les téléphones récupèrent la mise à jour au prochain contrôle (GitHub peut mettre jusqu'à 10 minutes à la diffuser).

**Sans compte GitHub : Netlify Drop** (https://app.netlify.com/drop)
1. Glissez le dossier `baiboly-contenu` (décompressé) dans la zone de dépôt : vous obtenez une adresse `https://xxxx.netlify.app`.
2. Créez un compte gratuit et « réclamez » le site (claim) pour qu'il reste en ligne.
3. Vérifiez que `https://xxxx.netlify.app/manifest.json` s'affiche, puis mettez `BASE_URL = "https://xxxx.netlify.app/"` dans `ContentUpdater.kt`.
4. Pour chaque mise à jour : dans la page du projet, glissez de nouveau le dossier mis à jour (avec le nouveau `manifest.json` et le nouveau `.db`).
Si le serveur est indisponible, l'application continue de fonctionner avec la Bible déjà installée.

Alternative : Firebase Hosting (même principe, avec `firebase deploy`).

## 3 bis. Mettre à jour l'APPLICATION elle-même, 100 % gratuit (sans Play Store)
Depuis la version 1.3.0, l'application vérifie `manifest.json` (bloc `"app"`) au lancement. S'il contient un `versionCode` plus grand que celui installé, une fenêtre « Version vaovao » propose de télécharger le nouvel APK, puis Android demande de confirmer l'installation. La première fois, Android demande aussi d'autoriser l'installation depuis l'application.

**IMPORTANT : la clé de signature.** Android n'installe une mise à jour que si l'APK est signé avec **la même clé** que celui déjà installé. Créez UNE clé et gardez-la pour toujours (copie sur clé USB + autre endroit sûr). Si vous la perdez, il faudra que tout le monde désinstalle puis réinstalle.
1. Android Studio : `Build > Generate Signed App Bundle / APK > APK > Next`.
2. `Create new...` : choisissez un fichier `.jks`, deux mots de passe (notez-les), un alias (ex. `jkmm`), puis OK.
3. Cochez `release`, puis Finish. L'APK est dans `app/release/app-release.apk` ; renommez-le `Baiboly-JKMM-1.3.0.apk`.
4. Pour chaque nouvelle version : augmentez `versionCode` (et `versionName`) dans `app/build.gradle.kts`, refaites l'étape 1 avec **la même clé** (`Choose existing...`).

**Publier une nouvelle version :**
`python tools/publier_apk.py Baiboly-JKMM-1.3.0.apk 6 1.3.0 "Texte des nouveautés" baiboly-contenu`
puis glissez le dossier `baiboly-contenu` sur Netlify. (Sans Python : envoyez l'APK à Claude, qui prépare le dossier.)
Les membres peuvent aussi télécharger l'application depuis la page d'accueil du site.

Les téléphones qui ont une version **inférieure à 1.3.0** ne contiennent pas cette fonction : ils doivent installer l'APK 1.3.0 à la main une fois (et le désinstaller d'abord si Android refuse à cause de la signature).

## 4. Publier sur le Google Play Store
1. Compte développeur Play Console (25 $, une seule fois).
2. Dans Android Studio : `Build > Generate Signed App Bundle` (choisissez **AAB**). **Sauvegardez précieusement le fichier .jks et son mot de passe** : sans lui, plus aucune mise à jour de l'application n'est possible.
3. Envoyez l'AAB dans la Play Console, ajoutez icône, captures d'écran et description.

Une mise à jour du **code** (nouvelles fonctions) passe par le Play Store : augmentez `versionCode` dans `app/build.gradle.kts`.

## 5. Limites connues de cette première version
- Le projet n'a pas pu être compilé là où il a été écrit : une petite erreur de compilation est possible au premier lancement. Copiez le message d'erreur d'Android Studio et on la corrige.
- Si une version plus récente de la base est incluse dans un futur APK alors qu'une base téléchargée existe déjà, c'est la base téléchargée qui reste utilisée.
- Texte source : Mg1865 du projet libre scrollmapper/bible_databases ; une relecture est conseillée (coquilles possibles).
