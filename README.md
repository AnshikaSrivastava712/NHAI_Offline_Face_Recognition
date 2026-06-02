# NHAI Offline Face Recognition & Liveness Detection
## Overview
This project is a mobile-based offline facial recognition and liveness detection system developed as a prototype for the NHAI Hackathon.
The solution is designed for remote locations where internet connectivity is unavailable. It enables secure user authentication using on-device facial recognition and basic liveness verification without requiring any network connection.
## Problem Statement
Develop a secure, lightweight, and fully offline facial recognition and liveness detection system that can operate on standard mobile devices in zero-network environments.
## Features
### Offline Face Registration
* Capture and register user facial data locally.
* No internet connection required.
### Offline Face Recognition
* Recognize registered users directly on the device.
* TensorFlow Lite based inference.
### Liveness Detection
* Blink detection.
* Head movement verification.
* Prevents basic spoofing using photos or screens.
### Local Database Storage
* User information stored locally.
* Encrypted storage support.
### Lightweight Edge AI
* MobileFaceNet TensorFlow Lite model.
* Optimized for mobile deployment.
## Technology Stack
* Android
* Java
* TensorFlow Lite
* ML Kit Face Detection
* CameraX
* SQLite / Room Database
* MobileFaceNet
## Project Structure

```text
app/
├── database/
├── facedetection/
├── facerecognition/
├── liveness/
├── assets/
│   └── mobilefacenet.tflite
└── MainActivity.java
## Workflow
1. User opens the application.
2. Face is detected using the device camera.
3. Liveness checks are performed:
   * Blink Detection
   * Head Pose Verification
4. Face embedding is generated.
5. Embedding is matched against locally stored users.
6. Authentication result is displayed.
## Security Features
* Offline operation.
* No dependency on cloud services.
* Local encrypted data storage.
* Basic anti-spoofing using liveness checks.
## Performance Goals
* Offline operation.
* Fast on-device inference.
* Designed for mid-range Android devices.
* Suitable for remote and low-connectivity regions.
## Future Enhancements
* React Native integration for Android and iOS.
* AWS synchronization after connectivity restoration.
* Automatic local data purge after successful sync.
* Improved liveness detection.
* Model compression and optimization.
* Attendance logging and reporting.
## Hackathon Prototype Disclaimer
This repository contains a prototype developed for demonstration and evaluation purposes during the NHAI Hackathon. Some advanced production features such as cloud synchronization and cross-platform deployment are proposed as future enhancements.

## Author
Anshika Srivastava
B.Tech Computer Science & Engineering
National Institute of Technology Patna
