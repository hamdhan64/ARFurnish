# 🚀 AR Furniture Placement App — Setup Guide

This is an Android AR application built with **Kotlin + Jetpack Compose + ARCore**.  
Follow the steps below to download, set up, and run the project.

---

## 📦 PART 1 — Required Setup

### 🔹 Step 1: Install Android Studio

Android Studio is required to run this project. It includes the IDE, SDK, and build tools.

Download Android Studio:  
https://developer.android.com/studio

- Install using default settings  
- Make sure Android SDK is included  
- First launch may take a few minutes  

---

### 🔹 Step 2: Install Required SDKs

Go to:  
File > Settings > Appearance & Behavior > System Settings > Android SDK

In **SDK Platforms**, ensure:
- Android 14 (API 34) ✔ required  
- Android 7 (API 24) ✔ optional  

In **SDK Tools**, ensure:
- Android SDK Build-Tools 34  
- Android SDK Platform-Tools  
- Android Emulator (optional)  

Click **Apply → OK**

---

### 🔹 Step 3: Java (JDK)

Android Studio includes a built-in JDK.

If needed:

File > Settings > Build, Execution, Deployment > Build Tools > Gradle  
Set **Gradle JDK → Android Studio Default**

---

## 📦 PART 2 — Download 3D Models

The `.glb` 3D models are not included in this repository due to file size.

Download them from Google Drive.

After downloading:

- Extract the folder if needed  
- You will get multiple `.glb` files  

Place them here: **app/src/main/assets/models/**
If folders don’t exist, create them manually.

⚠️ Without these files, AR features will not work properly.

---

## 📦 PART 3 — Open & Build Project

### 🔹 Step 4: Open Project

- Open Android Studio  
- Click **Open Project**  
- Select project folder  
- Wait for Gradle sync  

---

Clean Project (if errors occur)
### 🔹 Step 5 — Build the Project

#### 🔧 Debug Build (Testing) 
./gradlew assembleRelease 

## Clean Project (if errors occur)
./gradlew clean


## Install on Device / Emulator
./gradlew installDebug

## PART 📦 4 — Using the App

Launch the app
- Grant camera permission
- Point device at a flat surface
- Move slowly to detect surface
- Tap to place furniture
- Use UI controls to switch models



