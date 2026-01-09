# Notify-android

**Notify (Notion Lite)** — a small Android app for **notes + tasks** that works **offline-first** and syncs when online.  
Built for low-end phones and weak internet.

- **Tech:** Kotlin, Jetpack Compose, MVVM, Hilt, Room, WorkManager, Firebase Auth + Firestore
- **Target users:** small student teams 
- **Status:** MVP in progress (comments/mentions = optional/stretch)

---

## ✨ Features (MVP)

- **Spaces** (team workspaces)
- **Pages** (markdown-lite text + checklist, small attachments ≤5MB)
- **Tasks** (Todo/Doing/Done, assignee, due date, labels)
- **Search & filters**
- **Offline cache + sync** (local first, push/pull later)
- **Local due reminders** (WorkManager, reboot-safe)

**Stretch (optional):** Comments & @mentions, FCM mention notifications, Activity/History UI.

---

## 🧱 Tech Stack

- **Frontend:** Kotlin + **Jetpack Compose**, Material3
- **Architecture:** **MVVM**, **Hilt** DI, Coroutines + Flow
- **Local storage:** **Room**
- **Background:** **WorkManager** (sync, reminders)
- **Auth & backend:** **Firebase Auth** + **Firestore** (Storage optional for attachments)
- **CI:** GitHub Actions (build & test, upload debug APK)

---

## 🛠 Requirements

- **Android Studio** (Stable channel)
- **Gradle JDK:** **17** (IDE can run on JBR 21; Gradle must use 21)
  - Android Studio → *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK = 17*
- **Android SDK:** Platform **34**, Build-Tools **34.0.0**


## 🚀 Quick Start
git clone https://github.com/sophireaksoeng/Notify-android.git
cd Notify-android