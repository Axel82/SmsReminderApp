# 📱 Rappels SMS — Application Android de rappels programmés par SMS

Application Android native en **Kotlin** et **Jetpack Compose** permettant de planifier des rappels périodiques avec déclenchement de notifications interactives pour envoyer des SMS à vos contacts.

Le projet est conçu pour fonctionner **100 % hors-ligne** (aucune donnée n'est transmise à des serveurs tiers) avec un stockage local au format **JSON** et une chaîne d'intégration continue **GitHub Actions** produisant automatiquement les APK signés.

---

## 🚀 Fonctionnalités principales

### 1. 📅 Gestion des rappels (Stockage 100 % local)
- Stockage interne au format JSON (`reminders.json` dans `context.filesDir`).
- **Champs d'un rappel** :
  - **Titre** : Intitulé clair (ex: *Point d'équipe*, *Médicaments*, *Course*).
  - **Jours de la semaine** : Sélecteur multiple (Lundi à Dimanche) avec raccourcis (*Tous les jours*, *En semaine*, *Week-end*).
  - **Heure précise** : Sélecteur d'heure au format 24h.
  - **Destinataire** : Saisie manuelle du numéro ou sélection directe depuis les **Contacts** du téléphone via un sélecteur natif.
  - **Corps du SMS** : Message à envoyer avec compteur de caractères et estimation du nombre de SMS.
- Activation / Désactivation unitaire, modification et suppression de chaque rappel avec dialogue de confirmation.

### 2. ⚡ Fonctionnement fiable en arrière-plan
- **AlarmManager avec réveil (`setExactAndAllowWhileIdle`)** : Déclenchement à la seconde exacte même lorsque le téléphone est en veille profonde (Doze Mode).
- **Reprogrammation au redémarrage (`BOOT_COMPLETED`)** : Toutes les alarmes sont automatiquement reprogrammées dès le redémarrage du smartphone.
- **Bouton ON/OFF Global** : Permet de suspendre ou réactiver l'ensemble du système en un clic.
- **Gestion de l'optimisation de batterie** : Détection et demande d'exemption (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) pour éviter que le système Android ne coupe le service.

### 3. 🔔 Notifications interactives
- Notification avec canal de **haute priorité**, sonnerie et vibration.
- Affiche le nom/numéro du destinataire et le texte du SMS.
- **Actions rapides directement depuis la notification** :
  - **Envoyer** : Déclenche l'envoi du SMS (soit via l'application SMS par défaut, soit en direct en tâche de fond selon vos préférences) et enregistre l'événement dans le journal.
  - **Annuler** : Ferme la notification sans envoyer de SMS et consigne l'annulation dans le journal.

### 4. 📜 Journal des envois (Logs JSON)
- Fichier local `history_logs.json`.
- Horodatage complet, titre du rappel, destinataire, message, et statut (**Envoyé**, **Annulé**, **Échoué**).
- Onglet dédié avec recherche, filtres par statut et option pour vider l'historique.

### 5. 🎨 Interface moderne Material Design 3
- Interface 100 % Jetpack Compose fluide et réactive.
- Support natif du **Mode Sombre** (Dark Mode) et des couleurs dynamiques (Material You).
- Navigation ergonomique par barre d'onglets inférieure (**Rappels**, **Journal**, **Réglages**).

---

## 🏗️ Architecture du projet

```
SmsReminderApp/
├── .github/workflows/
│   └── build-apk.yml              # Pipeline CI/CD GitHub Actions
├── app/
│   ├── build.gradle.kts           # Dépendances Jetpack Compose, Material3, Coroutines, Gson
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml    # Permissions, Receivers & Activity
│       ├── java/com/smsreminder/app/
│       │   ├── SmsReminderApplication.kt
│       │   ├── model/             # Modèles de données (Reminder, DayOfWeek, HistoryLog, AppSettings)
│       │   ├── data/              # Service JSON & Repositories (JSON thread-safe)
│       │   ├── service/           # AlarmScheduler, NotificationHelper, SmsSender
│       │   ├── receiver/          # ReminderAlarmReceiver, SmsActionReceiver, BootReceiver
│       │   └── ui/
│       │       ├── MainActivity.kt
│       │       ├── navigation/    # Navigation par onglets
│       │       ├── screens/       # RemindersScreen, AddEditReminderDialog, HistoryScreen, SettingsScreen
│       │       ├── components/    # DaySelector, MasterSwitchCard, StatusBadge, etc.
│       │       └── theme/         # Color, Theme, Type Material 3
│       └── res/                   # Drawables, icônes adaptatives, chaînes
├── gradle/
│   ├── libs.versions.toml         # Version catalog Gradle
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔒 Permissions requises

| Permission | Rôle |
|---|---|
| `android.permission.POST_NOTIFICATIONS` | Afficher les notifications interactives (Android 13+) |
| `android.permission.SEND_SMS` | Envoyer les SMS programmés |
| `android.permission.READ_CONTACTS` | Ouvrir le carnet d'adresses pour sélectionner un destinataire |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Reprogrammer les rappels au démarrage du smartphone |
| `android.permission.SCHEDULE_EXACT_ALARM` | Déclencher les alertes à l'heure exacte |
| `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Assurer la fiabilité du réveil en veille |

---

## 📦 Pipeline CI/CD (GitHub Actions)

Le fichier [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) automatise l'intégralité du cycle de build :

1. **À chaque Push / Pull Request** :
   - Compile le projet avec JDK 17 et Gradle.
   - Génère les fichiers `SmsReminder-Debug.apk` et `SmsReminder-Release.apk`.
   - Publie les fichiers APK comme **Artifacts** téléchargeables pendant 30 jours.

2. **À chaque Tag de version (ex: `git tag v1.0.0 && git push origin v1.0.0`)** :
   - Génère automatiquement une **GitHub Release** officielle avec notes de version.
   - Attache les fichiers APK prêts à être installés directement sur smartphone.

---

## 🛠️ Compilation locale

### Prérequis
- **JDK 17** installé et configuré (`JAVA_HOME`).
- **Android SDK** (API 26 minimum, API 35 recommandée).
- **Android Studio** (Koala / Ladybug ou version ultérieure).

### Commandes Gradle
```bash
# Compiler l'APK de Debug
./gradlew assembleDebug

# Compiler l'APK de Release
./gradlew assembleRelease

# Exécuter les tests unitaires
./gradlew test
```

Les APK générés se trouvent dans :
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
