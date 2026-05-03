# RheoTV Android App

An Android application for the RheoTV platform — a live streaming and social video experience for gaming and entertainment communities.

## Overview

RheoTV is a feature-rich Android app (package: `com.rheotv.android`) built with Java and Kotlin. The app provides a comprehensive live streaming experience with features including live stream playback, content creation, social interactions, gamification, and monetization.

**Current Version:** 4.6.1 (Version Code: 461)

## Features

- **Live Streaming** — Watch and host live streams with a dedicated stream player and fullscreen support
- **Stories** — Create and view ephemeral story content with templates and customization tools
- **Audio Rooms** — Join and host audio-only live rooms
- **Clips** — Short-form video content creation and viewing
- **Moments** — Capture and share special in-stream moments
- **Video Trimmer** — Built-in video trimming tool for content editing
- **Image Cropper** — Crop and edit images before posting
- **Social Features** — Follow/follower system, user profiles, and social interactions
- **Gamification** — Leaderboards, rankings, scoreboards, and game selection
- **In-App Billing** — Subscription and purchase support via Google Play Billing
- **Wallet** — In-app wallet for virtual currency and transactions
- **Search** — Discover content and users
- **Push Notifications** — Real-time notifications via Firebase Cloud Messaging
- **Onboarding** — Guided first-run experience for new users

## Tech Stack

- **Languages:** Java & Kotlin
- **Architecture:** MVVM with Repository pattern
- **Dependency Injection:** Dagger 2
- **Networking:** Retrofit + gRPC (Protocol Buffers)
- **Database:** Room (local persistence)
- **Image Loading:** Glide, Picasso
- **Video Playback:** ExoPlayer (custom player implementation)
- **Navigation:** Jetpack Navigation Component
- **Analytics:** Mixpanel, Segment, Firebase Analytics, Firebase Performance
- **Crash Reporting:** Firebase Crashlytics
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Charts & Graphs:** MPAndroidChart
- **Deep Linking:** Branch.io
- **Customer Support:** Freshchat
- **Engagement:** MoEngage

## Project Structure

```
rheo-main/
├── app/                          # Main application module
│   └── src/main/java/com/rheotv/android/
│       ├── app/                  # Application class (RheoTvApp)
│       ├── data/                 # Data sources and repositories
│       ├── db/                   # Room database and DAOs
│       ├── di/                   # Dagger dependency injection modules
│       ├── factories/            # ViewModelFactory and other factories
│       ├── helpers/              # Helper classes and utilities
│       ├── model/                # Data models and entities
│       ├── player/               # Custom video player implementation
│       ├── services/             # Background services
│       ├── ui/                   # UI layer
│       │   ├── activities/       # Activities (home, player, profile, etc.)
│       │   ├── adapters/         # RecyclerView adapters
│       │   ├── base/             # Base classes
│       │   ├── customViews/      # Custom UI components
│       │   ├── decorators/       # RecyclerView item decorators
│       │   └── fragments/        # Fragments
│       └── utils/                # Utility classes
├── story/                        # Story feature module
├── doubletapplayerview/          # Custom double-tap player view library
├── ffmpeg/                       # FFmpeg integration module
├── video_trimmer_library/        # Video trimming library module
└── chillingvanlib/               # Auxiliary library module
```

## Prerequisites

- **Android Studio** Arctic Fox or newer
- **JDK** 8 or higher
- **Android SDK** with API level 21+ (Android 5.0 Lollipop and above)
- **Google Services** — A valid `google-services.json` file placed in `app/`
- **Firebase** project configured with Analytics, Crashlytics, and Performance Monitoring enabled

## Getting Started

1. **Clone the repository:**
   ```bash
      git clone https://github.com/ulohani90/rheo-main.git
         cd rheo-main
            ```

            2. **Add configuration files:**
               - Place your `google-services.json` in the `app/` directory
                  - Update `BASE_URL` in `app/build.gradle` to point to your API server

                  3. **Open in Android Studio:**
                     - Open Android Studio and select **Open an existing project**
                        - Navigate to the cloned `rheo-main` directory

                        4. **Build the project:**
                           ```bash
                              ./gradlew assembleDebug
                                 ```

                                 5. **Run on a device or emulator** using Android Studio, or via:
                                    ```bash
                                       ./gradlew installDebug
                                          ```

                                          ## Build Variants

                                          | Variant | Description |
                                          |---------|-------------|
                                          | `debug`   | Development build pointing to the production API |
                                          | `release` | Production build with ProGuard/R8 minification and obfuscation |

                                          ## Permissions

                                          The app requires the following Android permissions:

                                          - `INTERNET` — Network access for streaming and API calls
                                          - `FOREGROUND_SERVICE` — Background playback and audio room services
                                          - `READ_PHONE_STATE` / `MODIFY_AUDIO_SETTINGS` — Audio management
                                          - `BLUETOOTH` — Bluetooth audio device support
                                          - `ACCESS_WIFI_STATE` — Network state detection
                                          - `VIBRATE` — Haptic notification feedback
                                          - `CAMERA` / `RECORD_AUDIO` — Live stream broadcasting
                                          - `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` — Media access

                                          ## Deep Linking

                                          The app supports deep links using the following URI schemes:

                                          - `rheo://open/...`
                                          - `rheotv://open/...`

                                          ## Contributing

                                          1. Fork the repository
                                          2. Create a feature branch: `git checkout -b feature/your-feature-name`
                                          3. Commit your changes: `git commit -m 'Add some feature'`
                                          4. Push to the branch: `git push origin feature/your-feature-name`
                                          5. Open a Pull Request

                                          ## License

                                          This project is proprietary software. All rights reserved.

                                          ---

                                          *Built with love by the RheoTV team*
