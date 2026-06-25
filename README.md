🚀 AR Furniture Placement App — Setup Guide

This is an Android AR application built with Kotlin + Jetpack Compose + ARCore.
Follow the steps below to download, set up, and run the project successfully.

📦 PART 1 — Required Setup
🔹 Step 1: Install Android Studio

Android Studio is required to run this project. It includes the IDE, SDK, and build tools.

Download Android Studio: https://developer.android.com/studio
Install it using default settings
Make sure Android SDK is included during installation
First launch may take a few minutes to download additional components

🔹 Step 2: Install Required SDKs
After opening Android Studio:

Go to:
File > Settings > Appearance & Behavior > System Settings > Android SDK
In SDK Platforms, ensure:
Android 14 (API 34) ✔ required
Android 7 (API 24) ✔ optional
In SDK Tools, ensure:
Android SDK Build-Tools 34
Android SDK Platform-Tools
Android Emulator (optional)

Click Apply → OK to install missing components.

🔹 Step 3: Java (JDK)
Android Studio includes a built-in JDK.

If build issues occur:

Go to:
Settings > Build, Execution, Deployment > Build Tools > Gradle
Set Gradle JDK → Android Studio Default

📦 PART 2 — Download 3D Models
The .glb 3D furniture models are not included in this repository due to large file size.

👉 Download them from the provided Google Drive link.

After downloading:

Extract the folder (if compressed)
You will find multiple .glb files (e.g., sofa.glb, chair.glb, etc.)
Place them in the following directory:
ARFurnish/
 └── app/
     └── src/
         └── main/
             └── assets/
                 └── models/
                     ├── sofa.glb
                     ├── chair.glb
                     ├── bookshelf.glb
                     └── ...

📌 If folders don’t exist, create them manually.

⚠️ Without these files, AR features may fail or models will not load.

📦 PART 3 — Open & Build Project

🔹 Step 4: Open Project
Open Android Studio
Click Open Project
Select the project folder
Wait for Gradle Sync to complete (may take a few minutes)

🔹 Step 5: Enable Device Connection
To run the app:

Enable Developer Options
Turn on USB Debugging
Connect device via USB
Accept permission prompt when shown

🔹 Step 6: Run the App
Option A — Android Studio
Select device from dropdown
Click ▶ Run button
Option B — Terminal
./gradlew installDebug

📦 PART 4 — Using the App
Grant camera permission
Point device at a flat surface
Move slowly to detect plane
Tap to place furniture
Use UI controls to switch models
