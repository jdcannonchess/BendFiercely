# Bend Fiercely

A stretching workout app for Android designed to help you achieve the pancake skill and improve your acro yoga flexibility.

## Features

- **Random Stretch Generation**: Exercises are selected using weighted probability based on their pancake score (effectiveness)
- **Two Session Types**:
  - **Shallow**: Quick stretches (30-90 seconds each)
  - **Deep**: Intensive holds (2-5 minutes each)
- **Bilateral Exercise Support**: Exercises that require both sides are automatically queued left, then right
- **Session Tracking**: View your history and track progress over time
- **Beautiful Timer UI**: Circular countdown with visual feedback
- **Rest Periods**: 15-second rest between stretches with audio chime notification

## Exercises Included

The app includes 19 carefully selected stretches targeting:
- Hip flexibility (pancake, straddle, splits)
- Hip rotation (90/90, pigeon, fire log)
- Hamstring flexibility (half splits, Jefferson curl)
- Compression strength (seated leg lift-offs)
- General mobility (butterfly, frog, happy baby)

Each exercise has a "pancake score" from 1-100 indicating its effectiveness for achieving the pancake stretch. Higher-scored exercises are recommended more frequently.

## Building the App

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Setup

1. Clone or download this project
2. Open in Android Studio
3. Sync Gradle files
4. Add a chime sound file (see below)
5. Run on device or emulator (API 26+)

### Adding the Chime Sound

The app plays a chime when each stretch completes. To add your own:

1. Find a soft meditation bell or singing bowl sound (1-3 seconds, MP3 or OGG format)
2. Rename it to `chime.mp3`
3. Place it in `app/src/main/res/raw/`

Recommended free sources:
- [Freesound](https://freesound.org) - search "meditation bell" or "singing bowl"
- [Pixabay](https://pixabay.com/sound-effects/) - search "bell" or "chime"

If no chime file is present, the app will fall back to the system notification sound.

## Project Structure

```
app/src/main/java/com/bendfiercely/
├── MainActivity.kt              # Entry point
├── data/
│   ├── Exercise.kt             # Exercise data model
│   ├── ExerciseRepository.kt   # Exercise data + weighted selection
│   ├── SessionDatabase.kt      # Room database
│   ├── SessionDao.kt           # Database queries
│   ├── StretchSession.kt       # Session entity
│   ├── SessionStretch.kt       # Individual stretch entity
│   └── SessionRepository.kt    # Data operations
├── ui/
│   ├── theme/                  # Material3 theme (warm amber palette)
│   ├── components/
│   │   └── TimerDisplay.kt     # Circular timer component
│   ├── screens/
│   │   ├── HomeScreen.kt       # Main menu
│   │   ├── SessionTypeScreen.kt # Shallow/Deep selection
│   │   ├── ActiveStretchScreen.kt # Timer and current exercise
│   │   ├── SummaryScreen.kt    # Post-session summary
│   │   ├── HistoryScreen.kt    # Session history list
│   │   └── SessionDetailScreen.kt # Individual session details
│   └── navigation/
│       └── Navigation.kt       # NavHost setup
├── viewmodel/
│   ├── StretchViewModel.kt     # Active session state
│   └── HistoryViewModel.kt     # History queries
└── util/
    └── SoundManager.kt         # Audio playback
```

## Weighted Selection Algorithm

Exercises are selected using quadratic weighting based on pancake score:

```
weight = pancakeScore²
```

This means:
- Score 100 exercise: weight = 10,000
- Score 30 exercise: weight = 900
- Ratio: ~11x more likely to select the high-scoring exercise

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Navigation**: Navigation Compose
- **Database**: Room
- **Architecture**: MVVM with StateFlow
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## License

Personal use project - feel free to fork and modify for your own stretching journey!

---

*Bend fiercely, stretch consistently, achieve the pancake!*

