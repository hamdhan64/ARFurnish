🪑 AR Furniture Placement App (Android + ARCore)

This is an Augmented Reality (AR) Android application built using Kotlin, Jetpack Compose, and ARCore.
The app allows users to place and visualize 3D furniture models in real-world environments using their camera.

🚀 Features
Real-time AR furniture placement using ARCore
Multiple 3D GLB furniture models
Jetpack Compose modern UI
Smooth rendering with SceneView AR integration
Touch-based object interaction in real space
Lightweight and modular architecture

🧰 Tech Stack
Kotlin (Android Development)
Jetpack Compose (UI)
ARCore (Augmented Reality)
SceneView (AR rendering engine)
Gradle (Build system)

📦 Prerequisites
Before running the project, make sure you have:

Android Studio Hedgehog (2023.1.1) or newer
JDK 17+ (usually bundled with Android Studio)
Android SDK Platform 34
A physical Android device with ARCore support (recommended)

🔗 ARCore supported devices:
https://developers.google.com/ar/devices

⚙️ Setup Instructions
1. Clone the repository
git clone https://github.com/your-username/ARFurnish.git

2. Open in Android Studio
Launch Android Studio
Click Open Project
Select the project folder
Wait for Gradle sync to complete
📱 Running the App
🔹 Recommended (Physical Device)
Enable Developer Options on your phone
Turn on USB Debugging
Connect device via USB
Select device in Android Studio
Click Run ▶
🔹 Emulator (Limited Support)

AR features may not work properly on emulators. Use only for UI testing.

🏗 Build Commands
Debug APK
./gradlew assembleDebug
Release APK
./gradlew assembleRelease
Install directly on device
./gradlew installDebug
⚠️ Important Notes
This project requires a real ARCore-supported device for full functionality
3D models are large assets and may be stored using Git LFS or external storage
First build may take time due to Gradle dependency downloads
📌 Summary

This project demonstrates how Augmented Reality can be used for interior visualization, allowing users to place and interact with virtual furniture in real-world environments.
