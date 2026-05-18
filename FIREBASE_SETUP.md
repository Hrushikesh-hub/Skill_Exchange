# ðŸ”¥ FIREBASE SETUP â€” READ BEFORE BUILDING

## âš ï¸ CRITICAL: You need `google-services.json` before building!

---

## Step 1: Create Firebase Project (2 minutes)

1. Open [https://console.firebase.google.com/](https://console.firebase.google.com/)
2. Click **"Add project"** â†’ Name: `SkillExchange` â†’ **Create project**

## Step 2: Register Android App

1. Click the **Android icon** (&lt;/&gt;)
2. Package name: **`com.example.skillexchangeapp`**
3. App nickname: `SkillExchange` â†’ **Register app**
4. **Download `google-services.json`**
5. Place it here: `app/google-services.json` (next to `app/build.gradle.kts`)
6. Click **Next â†’ Next â†’ Continue to console**

## Step 3: Enable Authentication

1. Firebase Console â†’ **Authentication** â†’ **Get started**
2. Enable **Email/Password** â†’ Save

## Step 4: Enable Firestore (Cloud Database)

1. Firebase Console â†’ **Firestore Database** â†’ **Create database**
2. Choose: **Start in test mode** â†’ Location: `asia-south1` â†’ Enable

## Step 5: Enable Realtime Database (Chat)

1. Firebase Console â†’ **Realtime Database** â†’ **Create Database**
2. Choose: **Start in test mode** â†’ Location: `asia-south1` â†’ Done

---

## Security Rules (Already set to test mode â€” open for 30 days)

Firestore Rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

Realtime DB Rules:
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

---

## Demo Login Credentials (Auto-seeded on first launch)

| Email              | Password | Role  |
|--------------------|----------|-------|
| rajesh@demo.com    | demo123  | Admin |
| priya@demo.com     | demo123  | User  |
| venkat@demo.com    | demo123  | User  |
| meena@demo.com     | demo123  | User  |
| suresh@demo.com    | demo123  | User  |
| kavitha@demo.com   | demo123  | User  |
| ravi@demo.com      | demo123  | User  |
| anitha@demo.com    | demo123  | User  |

---

## What Firebase Powers in This App

| Feature | Firebase Service |
|---------|-----------------|
| User login/registration | Firebase Authentication |
| All user data, needs, offers, swaps | Firestore (cloud sync) |
| Real-time chat messages | Firebase Realtime Database |
| Notifications, reviews, reports | Firestore |
| Works offline too | Room SQLite (local cache) |

---

## Build Commands

```bash
# Sync Gradle
./gradlew sync

# Run debug build
./gradlew assembleDebug

# Or just press Run in Android Studio
```


---
> Last updated: May 2026 - Verified working with Firebase SDK 33.x
