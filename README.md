# 💬 QuickChat – Android Real-Time Chat Application
QuickChat is a lightweight Android chat application that enables users to exchange messages in real time.  
Built using Android (Java) and Firebase, this project focuses on fast messaging, simple UI, and real-time data synchronization.

## 📁 Project Structure
```text
quickchat/
├── app/
│   ├── src/
│   │   ├── main/                                 # Main application source code
│   │   ├── androidTest/                          # Instrumentation tests
│   │   └── test/                                 # Unit tests
│   ├── build.gradle                              # App-level Gradle config
│   ├── google-services.json                      # Firebase configuration
│   └── proguard-rules.pro
│
├── gradle/
│   └── wrapper/                                  # Gradle wrapper files
│
├── .idea/                                        # Android Studio project settings
├── build.gradle                                  # Project-level Gradle config
├── settings.gradle
├── gradlew / gradlew.bat                         # Gradle scripts
├── .gitignore
└── README.md
```

## ✨ Features
- 💬 Real-time chat messaging
- 👥 One-to-one user communication
- 🔄 Live message synchronization via Firebase
- 📱 Native Android UI
- 🔐 Firebase backend configuration
- 🧪 Basic unit and instrumentation test structure

## 🧰 Tech Stack
- 🤖 Android (Java) — Native Android application development
- ☕ Java — Core programming language
- 🔥 Firebase — Realtime Database, Authentication & Backend Services
- 🛠️ Gradle — Build automation and dependency management
- 🧪 Android Studio — Primary IDE for development and debugging

## 🧩 Architecture Overview
QuickChat follows a client–cloud architecture, where the Android app communicates with Firebase to store and retrieve messages in real time.

### 🔁 Architecture Flow
```text
+--------------------+
|  Android App (UI)  |
|  Java Activities   |
+---------+----------+
          |
          |  Send / Receive Messages
          v
+--------------------+
|     Firebase       |
|  Realtime Database |
|  / Services        |
+--------------------+
          ^
          |
          |  Live Updates
+---------+----------+
| Other App Clients  |
|   (Users)          |
+--------------------+
```
### 🧠 Component Explanation
- 📱 Android App: Handles UI and user interactions
- 🔥 Firebase Realtime Database: Stores and syncs chat messages
- 🔄 Realtime Sync: Ensures instant message delivery across users

## ⚙️ How to Run the Project
### 🧱 Prerequisites  
Before running the project, ensure the following are set up:
- 💻 Android Studio (latest stable version recommended)
- ☕ Java JDK (compatible with Android Studio)
- 🔥 Firebase Account (for Realtime Database & services)
- 📱 Android Emulator or Physical Android Device

### ▶️ Run Instructions
- 1️⃣ Clone the repository
  ```bash
  git clone https://github.com/BeMaurya/quickchat.git
  ```
- 2️⃣ Open the project in Android Studio
- 3️⃣ Configure Firebase
  - Ensure `google-services.json` is present in the app/ directory
  - Update Firebase configuration if required
- 4️⃣ Sync Gradle files
- 5️⃣ Run the application ▶️
  - Select an emulator or connected device
  - Click Run in Android Studio

## ❤️ Contributions
Contributions are welcome!
> Fork the repo → Create a branch → Add feature → Submit PR

</br></br>
<div align="center">
<p>📘 This project is created strictly for educational and learning purposes.</p>
<p>⭐ If you find this project helpful, feel free to star the repository!</p>
<p>© 2026 <strong><a href = "https://bemaurya.github.io">BeMaurya</a></strong>. All rights reserved.</p>
</div>
