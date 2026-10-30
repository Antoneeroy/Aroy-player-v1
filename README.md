# Aroy Player — Video & MP3 Player (Online + Offline)

A fast, clean, and modern Android media player application built with **Jetpack Compose (Material 3)**, **AndroidX Media3 (ExoPlayer)**, and **Android Room Database**. Supports offline device storage scanning via **MediaStore API** as well as online video/audio streaming (HLS `.m3u8`, direct MP4, and live radio streams).

---

## 🌟 Core Features

### Phase 1: Offline Playback & Local Storage
- **Phone Storage Scanning**: Automatically scans device storage via Android MediaStore API for all video (`.mp4`, `.mkv`, `.avi`, `.webm`) and audio (`.mp3`, `.aac`, `.wav`, `.flac`) files.
- **Folder-Wise Browsing**: Categorizes videos and music by directories (Downloads, WhatsApp, Camera, Movies, Music, etc.) with file counts and quick "Play All".
- **Full-Screen Video Player**:
  - Immersive video view with hardware acceleration.
  - **Gesture Controls**:
    - Left-half vertical swipe: Screen brightness adjustment with on-screen HUD.
    - Right-half vertical swipe: Media volume adjustment with on-screen HUD.
    - Double-tap left: Fast rewind 10 seconds.
    - Double-tap right: Fast forward 10 seconds.
  - **Playback Speed Control**: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x.
  - **Aspect Ratio Toggles**: Fit Screen, Zoom/Crop, Stretch/Fill, 16:9, 4:3.
  - **Subtitle Support**: Load local `.srt` or `.vtt` subtitle files directly into the player.
  - **Screen Lock**: Prevents accidental touches while watching videos.
  - **Picture-in-Picture (PiP)**: Watch videos while multitasking in other apps.
- **Rich Audio & Music Player**:
  - Persistent **Mini-Player Bar** with progress bar, artwork, and quick playback controls.
  - Full-screen / expandable **Audio Sheet** with animated rotating vinyl disc, real-time equalizer wave animation, sleep timer, track scrubber, and repeat modes (Off, All, One).
  - Background audio playback capability.
- **Playback History & Smart Resume**:
  - Remembers exact playback position for every video and song in a Room SQLite database.
  - "Continue Playing" feed with visual progress indicators and one-tap resume.
- **Playlists & Queues**:
  - Create custom playlists (e.g. "Favorites", "Chill Vibes", custom named playlists).
  - Add any video or audio file to playlists directly from 3-dot menus.

### Phase 2: Online Streaming
- **Stream from URL**: Play direct HLS (`.m3u8`) adaptive bitrate streams, MP4 online videos, and live audio streams.
- **Pre-loaded Curated Legal Streams**:
  - Big Buck Bunny (Mux HLS 1080p stream)
  - Tears of Steel (Sci-Fi Full HD MP4)
  - Elephants Dream (Open movie stream)
  - Chill Lofi Live Radio (Live Icecast audio stream)
- **Saved Streams History**: Bookmark favorite online stream URLs with custom titles for instant replay.

### Theme & Settings
- **Theme Modes**: Cyber Dark, AMOLED Pure Black (battery saver for OLED screens), Light Theme, and System Default.
- **Sleep Timer**: Auto-stops playback after 15, 30, 45, 60, or 90 minutes.

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (Material 3)
- **Media Engine**: AndroidX Media3 (ExoPlayer 1.5.1, HLS extension, Media3 UI)
- **Database**: Android Room 2.7.0 (with KSP code generation)
- **Image Loading**: Coil Compose 2.7.0
- **Asynchronous**: Kotlin Coroutines & Flow
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 36

---

## 🚀 Build Instructions

### Prerequisites
- JDK 17 or higher
- Android SDK with Platforms 24 through 36 installed
- Gradle 8+

### Build Debug APK
```bash
gradle :app:assembleDebug
```
The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 🔒 Permissions & Safety
- `READ_MEDIA_VIDEO` & `READ_MEDIA_AUDIO` (Android 13+)
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `INTERNET` & `ACCESS_NETWORK_STATE` (for online stream playback)
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (for background audio playback)
- `POST_NOTIFICATIONS` (for media session notifications)
- Zero third-party trackers or ads.

---

## 👨‍💻 Developer Notes & Answers to Brief Questions

1. **ExoPlayer / Media3 experience**:
   - Implemented using AndroidX Media3 ExoPlayer 1.5.1 with HLS media source support, custom `PlayerView`, subtitle attachment, aspect ratio controls, and playback speed parameter configurations.
2. **Kotlin Compose App**:
   - 100% Jetpack Compose using Material 3 design system, Edge-to-Edge window insets, custom gesture detection, and MVVM architecture.
3. **Demo & APK**:
   - Fully built, tested, and verifiable inside the AI Studio streaming emulator with bundled sample videos/audios and curated streams ready to play immediately.
