# ðŸ¤ SkillExchange â€“ Barter-Based Skill Sharing Android App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![Room](https://img.shields.io/badge/Database-Room%20%2B%20SQLite-yellow.svg)](https://developer.android.com/training/data-storage/room)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-purple.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-red.svg)](LICENSE)

---

## ðŸ“Œ Problem Statement

In today's world, people possess a wide range of skills â€” coding, music, design, language, cooking â€” but lack a structured platform to **exchange** those skills without monetary transactions. Traditional learning platforms are expensive and inaccessible to many. **SkillExchange** solves this by enabling a **barter economy for knowledge**: you teach what you know, and learn what you need â€” for free.

---

## ðŸŒŸ Key Features

| Feature | Description |
|---|---|
| ðŸ” **Firebase Authentication** | Secure email/password login and registration |
| ðŸ“‹ **Skill Board** | Post skills you can offer and needs you want fulfilled |
| ðŸ” **Barter Swap System** | Propose, accept, reject, and track skill swaps |
| ðŸ’¬ **Real-time Chat** | Message other users directly within a swap |
| ðŸ§  **AI Match Suggestions** | Smart barter matching engine with compatibility scoring |
| ðŸ† **Trust Score & Gamification** | Reputation system with badges and skill points |
| ðŸ‘› **Skill Wallet** | Track earned and spent skill points per transaction |
| ðŸ”” **Notifications** | Real-time alerts for swap requests and messages |
| ðŸ“Š **Dashboard** | Live stats â€” active swaps, trust score, recent activity |
| ðŸ›¡ï¸ **Admin Panel** | Moderate users, verify skills, handle reports |
| ðŸ“ˆ **Impact Metrics** | Track community-wide learning impact |
| ðŸŒ™ **Dark/Light Theme** | Material You design with dynamic theming |

---

## ðŸ› ï¸ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 100% |
| **Platform** | Android (minSdk 26, targetSdk 36) |
| **UI** | XML Layouts + Material Design 3 + View Binding |
| **Architecture** | MVVM + Repository Pattern |
| **Local DB** | Room (SQLite) with 14 entities |
| **Cloud Backend** | Firebase Firestore + Firebase Realtime Database |
| **Authentication** | Firebase Auth |
| **Navigation** | Android Navigation Component |
| **Async** | Kotlin Coroutines + LiveData |
| **Build System** | Gradle (Kotlin DSL) |

---

## ðŸ“‚ Project Structure

```
SkillExchange-app/
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ src/
â”‚   â”‚   â””â”€â”€ main/
â”‚   â”‚       â”œâ”€â”€ java/com/example/skillexchangeapp/
â”‚   â”‚       â”‚   â”œâ”€â”€ MainActivity.kt               # Entry point & nav host
â”‚   â”‚       â”‚   â”œâ”€â”€ SkillExchangeApplication.kt   # App-level init
â”‚   â”‚       â”‚   â”œâ”€â”€ ui/
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ fragment/                 # 17 screen fragments
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ LoginFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ RegisterFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ DashboardFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ OfferFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ NeedFeedFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ SwapManagementFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ ChatFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ ProfileFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ WalletFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ NotificationFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ AdminFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ ImpactFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ HistoryFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ PostNeedFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ OfferManagementFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ SettingsFragment.kt
â”‚   â”‚       â”‚   â”‚   â”‚   â””â”€â”€ SplashFragment.kt
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ adapter/                  # RecyclerView adapters
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ viewmodel/                # ViewModels (MVVM)
â”‚   â”‚       â”‚   â”‚   â””â”€â”€ theme/                    # Dynamic theming
â”‚   â”‚       â”‚   â”œâ”€â”€ data/
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ local/
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ AppDatabase.kt        # Room DB (v5)
â”‚   â”‚       â”‚   â”‚   â”‚   â”œâ”€â”€ dao/                  # 14 DAOs
â”‚   â”‚       â”‚   â”‚   â”‚   â””â”€â”€ entity/               # 14 Room entities
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ firebase/                 # Firestore & RTDB sync
â”‚   â”‚       â”‚   â”‚   â”œâ”€â”€ repository/               # Repository layer
â”‚   â”‚       â”‚   â”‚   â””â”€â”€ source/                   # Data source abstractions
â”‚   â”‚       â”‚   â”œâ”€â”€ ai/                           # AI match engine
â”‚   â”‚       â”‚   â””â”€â”€ utils/                        # Helpers, seeders, extensions
â”‚   â”‚       â””â”€â”€ res/
â”‚   â”‚           â”œâ”€â”€ layout/                       # 33 XML layout files
â”‚   â”‚           â”œâ”€â”€ navigation/                   # Nav graph
â”‚   â”‚           â”œâ”€â”€ drawable/                     # Icons and graphics
â”‚   â”‚           â””â”€â”€ values/                       # Colors, strings, themes
â”‚   â”œâ”€â”€ build.gradle.kts                          # App-level Gradle config
â”‚   â””â”€â”€ google-services.json                      # Firebase config
â”œâ”€â”€ build.gradle.kts                              # Project-level Gradle
â”œâ”€â”€ settings.gradle.kts                           # Module settings
â”œâ”€â”€ gradle.properties                             # Gradle JVM args
â”œâ”€â”€ gradlew / gradlew.bat                         # Gradle wrapper
â””â”€â”€ README.md
```

---

## âš™ï¸ Setup & Installation

### Prerequisites
- Android Studio **Hedgehog** (2023.1.1) or newer
- JDK 11+
- Android SDK (API 26â€“36)
- A Firebase project (free tier works)

### Step 1 â€“ Clone the Repository

```bash
git clone https://github.com/Hrushikesh-hub/Skill_Exchange.git
cd Skill_Exchange
```

### Step 2 â€“ Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a project named **SkillExchange**
3. Add an Android app with package name `com.example.skillexchangeapp`
4. Download `google-services.json` and place it in `app/`
5. Enable **Email/Password** under Authentication â†’ Sign-in Methods
6. Enable **Firestore Database** and **Realtime Database** in test mode

### Step 3 â€“ Open in Android Studio

```
1. Launch Android Studio
2. File â†’ Open â†’ select the cloned folder
3. Wait for Gradle sync to complete
4. Connect an Android device (API 26+) or start an emulator
```

### Step 4 â€“ Run the App

```bash
# Via Android Studio:
Run â†’ Run 'app'  (Shift + F10)

# Via Gradle CLI:
./gradlew installDebug
```

### Step 5 â€“ Build APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## ðŸ“± App Screens & Usage

### ðŸ” Authentication
- Launch the app â†’ **Splash Screen** auto-navigates to Login
- New users â†’ tap **Register**, fill name/email/password/skills
- Existing users â†’ **Login** with email and password

### ðŸ“‹ Dashboard
- View your **Trust Score**, active swaps, skill points
- Quick stats: total offers, completed swaps, pending needs

### ðŸŽ¯ Post a Skill Offer
- Navigate to **Offer** tab â†’ Fill skill name, description, category, availability
- Tap **Submit** â†’ Offer appears on the community board

### ðŸ” Browse & Request Skills (Need Feed)
- Browse all posted skill offers
- Tap **Request Swap** on any offer to initiate a barter

### ðŸ” Swap Management
- View all **Pending / Active / Completed** swaps
- Accept or reject incoming requests
- Mark swaps as completed to earn skill points

### ðŸ’¬ Chat
- Open any active swap â†’ **Message** the other user in real time

### ðŸ‘› Wallet
- Track your **Skill Points** earned and spent
- View complete transaction history

---

## ðŸ§ª Demo Credentials (for evaluators)

```
Email:    demo@skillexchange.com
Password: Demo@1234
```
> The app auto-seeds demo data on first launch for easy evaluation.

---

## ðŸ“¸ Screenshots

| Splash & Login | Dashboard | Skill Feed |
|---|---|---|
| ![splash](screenshots/splash.png) | ![dashboard](screenshots/dashboard.png) | ![feed](screenshots/feed.png) |

| Offer Screen | Swap Management | Profile |
|---|---|---|
| ![offer](screenshots/offer.png) | ![swap](screenshots/swap.png) | ![profile](screenshots/profile.png) |

> Screenshots directory: [`/screenshots`](./screenshots/)

---

## ðŸ—ï¸ Architecture Overview

```
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚              UI Layer               â”‚
â”‚  Fragments + ViewModels + Adapters  â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                 â”‚ LiveData / StateFlow
â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚          Repository Layer           â”‚
â”‚  Coordinates Local DB + Firebase    â”‚
â””â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¬â”€â”€â”€â”€â”€â”€â”€â”˜
       â”‚                      â”‚
â”Œâ”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â–¼â”€â”€â”€â”€â”€â”€â”€â”€â”
â”‚  Room (Local)â”‚   â”‚  Firebase Cloud   â”‚
â”‚  SQLite DB   â”‚   â”‚  Firestore + RTDB â”‚
â”‚  14 Entities â”‚   â”‚  Auth + Sync      â”‚
â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

---

## ðŸ”® Future Improvements

- [ ] **Video Call Integration** â€“ Real-time video sessions for skill teaching
- [ ] **AI Personalized Recommendations** â€“ ML-based skill match scoring
- [ ] **Push Notifications** â€“ FCM for real-time alerts even when app is closed
- [ ] **Skill Verification** â€“ Community-voted skill endorsements
- [ ] **Group Skill Circles** â€“ Multi-user barter groups
- [ ] **Offline Mode** â€“ Full offline-first with background sync
- [ ] **iOS Version** â€“ Flutter port for cross-platform support
- [ ] **Web Dashboard** â€“ Admin portal via React

---

## ðŸ¤ Contributing

```bash
# Fork the repo
# Create feature branch
git checkout -b feature/your-feature-name

# Commit changes
git commit -m "feat: add your feature"

# Push and open Pull Request
git push origin feature/your-feature-name
```

---

## ðŸ“„ License

This project is licensed under the **MIT License** â€“ see the [LICENSE](LICENSE) file for details.

---

## ðŸ‘¨â€ðŸ’» Author

**Hrushikesh M**  
Final Year Computer Science Engineering Student  
ðŸ“§ [cooldestinyrockers@gmail.com](mailto:cooldestinyrockers@gmail.com)

---

## â­ Star this repo if it helped you!

> *"The best way to learn is to teach."* â€“ SkillExchange makes that possible.


---
> Built for MindMatrix VTO Internship Program — Project 25


